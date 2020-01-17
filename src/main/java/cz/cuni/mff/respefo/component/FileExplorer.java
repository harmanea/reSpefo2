package cz.cuni.mff.respefo.component;

import cz.cuni.mff.respefo.SpefoException;
import cz.cuni.mff.respefo.format.FormatManager;
import cz.cuni.mff.respefo.function.FunctionInfo;
import cz.cuni.mff.respefo.function.FunctionManager;
import cz.cuni.mff.respefo.function.MultiFileFunction;
import cz.cuni.mff.respefo.function.SingleFileFunction;
import cz.cuni.mff.respefo.resources.ImageResource;
import cz.cuni.mff.respefo.util.Message;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.widgets.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.IntFunction;

import static cz.cuni.mff.respefo.resources.ImageManager.getImage;
import static cz.cuni.mff.respefo.resources.ImageResource.*;
import static cz.cuni.mff.respefo.util.utils.FileUtils.getFileExtension;
import static java.util.stream.Collectors.toList;
import static org.eclipse.swt.SWT.*;

public class FileExplorer {
    private final Tree tree;
    private File rootDirectory;

    public FileExplorer(Composite parent) {
        tree = new Tree(parent, BORDER | MULTI | V_SCROLL);

        addExpandListener();
        addCollapseListener();

        new FileExplorerMenu(tree);
    }

    public void setLayoutData(Object layoutData) {
        tree.setLayoutData(layoutData);
    }

    public void setRootDirectory(File file) throws SpefoException {
        if (file == null) {
            throw new SpefoException("File is null.");
        } else if (!file.isDirectory()) {
            throw new SpefoException("File is not a directory.");
        } else if (!file.exists()) {
            throw new SpefoException("File doesn't exist.");
        }

        clearTree();

        File[] children = file.listFiles();
        Arrays.sort(children, (a, b) -> a.getName().compareToIgnoreCase(b.getName()));
        for (File child : children) {
            TreeItem item = new TreeItem(tree, 0);
            setUpChildItem(item, child);
        }

        rootDirectory = file;
    }

    public File getRootDirectory() {
        return rootDirectory;
    }

    public void refresh() {
        TreeItem[] items = tree.getItems();

        refreshItems(items, rootDirectory, index -> new TreeItem(tree, 0, index));
    }

    private void refreshNested(TreeItem item, File file) {
        if (file.isDirectory()) {
            int nestedFileCount = file.list().length;

            if (nestedFileCount == 0 && item.getItemCount() > 0) {
                if (item.getExpanded()) {
                    item.setImage(getImage(FOLDER));
                }

                for (TreeItem treeItem : item.getItems()) {
                    treeItem.dispose();
                }

            } else if (item.getExpanded()) {
                refreshItems(item.getItems(), file, index -> new TreeItem(item, 0, index));

            } else if (nestedFileCount > 0 && item.getItemCount() == 0) {
                new TreeItem(item, 0);
            }
        }
    }

    private void refreshItems(TreeItem[] items, File file, IntFunction<TreeItem> itemFactory) {
        File[] checkFiles = file.listFiles();

        Arrays.sort(checkFiles, (a, b) -> a.getName().compareToIgnoreCase(b.getName()));

        int itemsIndex = 0;
        int filesIndex = 0;

        while (itemsIndex < items.length && filesIndex < checkFiles.length) {
            final TreeItem treeItem = items[itemsIndex];

            final File itemFile = (File) treeItem.getData();
            final File checkFile = checkFiles[filesIndex];

            int comparison = checkFile.getName().compareToIgnoreCase(itemFile.getName());
            if (comparison == 0) {
                // same file -> recursively refresh as well

                refreshNested(treeItem, checkFile);

                itemsIndex++;
                filesIndex++;

            } else if (comparison < 0) {
                // new file found that should appear before the current file -> insert it

                TreeItem newTreeItem = itemFactory.apply(filesIndex);
                setUpChildItem(newTreeItem, checkFile);

                filesIndex++;

            } else /* (comparison > 0) */ {
                // new file should appear after current file, it is stale -> delete it

                treeItem.dispose();

                itemsIndex++;
            }
        }
        while (itemsIndex < items.length) {
            // dispose extra stale files at the end

            items[itemsIndex].dispose();

            itemsIndex++;
        }
        while (filesIndex < checkFiles.length) {
            // add extra files add the end

            TreeItem newTreeItem = itemFactory.apply(filesIndex);
            setUpChildItem(newTreeItem, checkFiles[filesIndex]);

            filesIndex++;
        }
    }

    public List<File> getSelection() {
        return Arrays.stream(tree.getSelection())
                .map(item -> (File) item.getData())
                .collect(toList());
    }

    public void collapseAll() {
        tree.setRedraw(false);

        for (TreeItem item : tree.getItems()) {
            if (((File) item.getData()).isDirectory()) {
                item.setExpanded(false);
                item.setImage(getImage(FOLDER));
            }
        }

        tree.setRedraw(true);
        tree.redraw();
    }

    private void clearTree() {
        for (TreeItem treeItem : tree.getItems()) {
            treeItem.dispose();
        }
    }

    private void addExpandListener() {
        tree.addListener(Expand, event -> expandTreeItem((TreeItem) event.item));
    }

    private void addCollapseListener() {
        tree.addListener(Collapse, event -> collapseTreeItem((TreeItem) event.item));
    }

    private void expandTreeItem(TreeItem item) {
        tree.setRedraw(false);

        for (TreeItem childItem : item.getItems()) {
            childItem.dispose();
        }

        File file = (File) item.getData();

        File[] children = file.listFiles();
        Arrays.sort(children, (a, b) -> a.getName().compareToIgnoreCase(b.getName()));
        for (File child : children) {
            TreeItem childItem = new TreeItem(item, 0);
            setUpChildItem(childItem, child);
        }

        item.setImage(getImage(OPENED_FOLDER));

        tree.setRedraw(true);
    }

    private static void setUpChildItem(TreeItem item, File file) {
        item.setText(file.getName());
        item.setData(file);

        if (file.isDirectory() && file.list().length > 0) {
            new TreeItem(item, 0);
        }

        item.setImage(getImage(getImageResourceForFile(file)));
    }

    private static void collapseTreeItem(TreeItem item) {
        item.setImage(getImage(FOLDER));
    }

    private static ImageResource getImageResourceForFile(File file) {
        if (file.isDirectory()) {
            return FOLDER;
        } else {
            String fileExtension = getFileExtension(file);

            if (fileExtension.equals("spf")) {
                return SPECTRUM_FILE;
            } else if (FormatManager.getKnownFileExtensions().contains(fileExtension)) {
                return IMPORTABLE_FILE;
            } else if (fileExtension.equals("stl") || fileExtension.equals("lst")) {
                return SUPPORT_FILE;
            } else {
                return FILE; // TODO: add more custom icons
            }
        }
    }

    public class FileExplorerMenu {
        private final Menu menu;

        private List<MenuItem> contextOptions = new ArrayList<>();

        FileExplorerMenu(Tree tree) {
            menu = new Menu(tree);
            tree.setMenu(menu);

            createAlwaysVisibleMenuItems();

            menu.addMenuListener(new MenuAdapter() {
                @Override
                public void menuShown(MenuEvent e) {
                    super.menuShown(e);

                    List<File> selection = getSelection();

                    contextOptions.forEach(Widget::dispose);
                    contextOptions.clear();

                    if (selection.size() == 1) {
                        createSingleSelectionMenuItems(selection.get(0));
                    } else if (selection.size() > 1) {
                        createMultiSelectionMenuItems(selection);
                    }

                    if (contextOptions.isEmpty()) {
                        MenuItem item = new MenuItem(menu, 0);
                        item.setText("No actions available");
                        item.setEnabled(false);
                        contextOptions.add(item);
                    }
                }
            });
        }

        private void createAlwaysVisibleMenuItems() {
            MenuItem item = new MenuItem(menu, 0);
            item.setText("Change Directory");
            item.addListener(Selection, event -> {
                DirectoryDialog dialog = new DirectoryDialog(menu.getShell());

                dialog.setText("Choose directory");
                dialog.setFilterPath(getRootDirectory().getPath());

                String directoryName = dialog.open();

                if (directoryName != null) {
                    try {
                        setRootDirectory(new File(directoryName));
                    } catch (Exception exception) {
                        Message.error("Couldn't change directory.", exception);
                    }
                }
            });

            item = new MenuItem(menu, 0);
            item.setText("Refresh");
            item.addListener(Selection, event -> refresh());

            item = new MenuItem(menu, 0);
            item.setText("Collapse All");
            item.addListener(Selection, event -> collapseAll());

            new MenuItem(menu, SEPARATOR);
        }

        private void createSingleSelectionMenuItems(File selection) {
            for (FunctionInfo<SingleFileFunction> functionInfo : FunctionManager.getSingleFileFunctions()) {
                if (functionInfo.getFileFilter().accept(selection)) {

                    MenuItem item = new MenuItem(menu, 0);
                    item.setText(functionInfo.getName());
                    item.addListener(Selection, event -> functionInfo.getInstance().execute(selection));

                    contextOptions.add(item);
                }
            }
        }

        private void createMultiSelectionMenuItems(List<File> selection) {
            for (FunctionInfo<MultiFileFunction> functionInfo : FunctionManager.getMultiFileFunctions()) {
                if (selection.stream().allMatch(file -> functionInfo.getFileFilter().accept(file))) {
                    MenuItem item = new MenuItem(menu, 0);
                    item.setText(functionInfo.getName());
                    item.addListener(Selection, event -> functionInfo.getInstance().execute(getSelection()));

                    contextOptions.add(item);
                }
            }
        }
    }
}
