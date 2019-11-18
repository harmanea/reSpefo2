package cz.cuni.mff.respefo.component;

import cz.cuni.mff.respefo.SpefoException;
import cz.cuni.mff.respefo.function.FunctionInfo;
import cz.cuni.mff.respefo.function.FunctionManager;
import cz.cuni.mff.respefo.function.MultiFileFunction;
import cz.cuni.mff.respefo.function.SingleFileFunction;
import cz.cuni.mff.respefo.resources.ImageManager;
import cz.cuni.mff.respefo.util.Message;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static cz.cuni.mff.respefo.resources.ImageResource.*;
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

    public Menu getMenu() {
        if (tree.getMenu() == null) {
            tree.setMenu(new Menu(tree));
        }

        return tree.getMenu();
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
        for (File child : file.listFiles()) {
            TreeItem item = new TreeItem(tree, 0);
            setUpChildItem(item, child);
        }

        rootDirectory = file;
    }

    public File getRootDirectory() {
        return rootDirectory;
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
                item.setImage(ImageManager.getImage(FOLDER));
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

        for (File child : file.listFiles()) {
            TreeItem childItem = new TreeItem(item, 0);
            setUpChildItem(childItem, child);
        }

        item.setImage(ImageManager.getImage(OPENED_FOLDER));

        tree.setRedraw(true);
    }

    private static void setUpChildItem(TreeItem item, File file) {
        item.setText(file.getName());
        item.setData(file);

        if (file.isDirectory() && file.list().length > 0) {
            new TreeItem(item, 0);
        }

        item.setImage(getImageForFile(file));
    }

    private static void collapseTreeItem(TreeItem item) {
        item.setImage(ImageManager.getImage(FOLDER));
    }

    private static Image getImageForFile(File file) {
        if (file.isDirectory()) {
            return ImageManager.getImage(FOLDER);
        } else {
            return ImageManager.getImage(FILE); // TODO: do this better
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
