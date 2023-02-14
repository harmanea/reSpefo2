package cz.cuni.mff.respefo.component;

import cz.cuni.mff.respefo.function.FunctionInfo;
import cz.cuni.mff.respefo.function.FunctionManager;
import cz.cuni.mff.respefo.function.MultiFileFunction;
import cz.cuni.mff.respefo.function.SingleFileFunction;
import cz.cuni.mff.respefo.function.filter.PlainTextFileFilter;
import cz.cuni.mff.respefo.logging.Log;
import cz.cuni.mff.respefo.resources.ColorManager;
import cz.cuni.mff.respefo.util.FileCopy;
import cz.cuni.mff.respefo.util.Message;
import cz.cuni.mff.respefo.util.Progress;
import cz.cuni.mff.respefo.util.utils.FileUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TreeEditor;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.*;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.IntFunction;

import static cz.cuni.mff.respefo.resources.ColorResource.BLACK;
import static cz.cuni.mff.respefo.resources.ImageManager.getIconForFile;
import static cz.cuni.mff.respefo.resources.ImageManager.getImage;
import static cz.cuni.mff.respefo.resources.ImageResource.FOLDER;
import static cz.cuni.mff.respefo.resources.ImageResource.OPENED_FOLDER;
import static cz.cuni.mff.respefo.util.utils.FileUtils.filenamesListToString;
import static cz.cuni.mff.respefo.util.utils.FileUtils.filesListToString;
import static java.util.stream.Collectors.toList;
import static org.eclipse.swt.SWT.*;

public class FileExplorer {

    private static FileExplorer defaultInstance;

    private final Tree tree;
    private final TreeEditor treeEditor;

    private final Clipboard clipboard;

    public static FileExplorer getDefault() {
        return defaultInstance;
    }

    public static void setDefaultInstance(FileExplorer defaultInstance) {
        FileExplorer.defaultInstance = defaultInstance;
    }

    public FileExplorer(Composite parent) {
        tree = new Tree(parent, MULTI | V_SCROLL | VIRTUAL);

        treeEditor = new TreeEditor(tree);
        treeEditor.horizontalAlignment = LEFT;

        addExpandListener();
        addCollapseListener();
        addDoubleClickListener();
        addKeyListener();

        clipboard = new Clipboard(parent.getDisplay());

        new FileExplorerMenu();
    }

    public void setLayoutData(Object layoutData) {
        tree.setLayoutData(layoutData);
    }

    public void setRootDirectory(File file) {
        clearTree();
        tree.setEnabled(false);

        Progress.withProgressTracking(p -> {
            File[] children = listFiles(file);
            p.refresh("Loading files", children.length);
            Arrays.sort(children, (a, b) -> a.getName().compareToIgnoreCase(b.getName()));

            for (File child : children) {
                p.asyncExec(() -> {
                    TreeItem item = new TreeItem(tree, NONE);
                    setUpChildItem(item, child);
                });
                p.step();
            }

            return null;
        }, n -> tree.setEnabled(true));
    }

    public void refresh() {
        if (tree.isEnabled()) {
            tree.setEnabled(false);
            refreshItems(tree.getItems(), Project.getRootDirectory(), index -> new TreeItem(tree, NONE, index));
            tree.setEnabled(true);
        }
    }

    private void refreshNested(TreeItem item, File file) {
        if (file.isDirectory()) {
            int nestedFileCount = listFiles(file).length;

            if (nestedFileCount == 0 && item.getItemCount() > 0) {
                // the file no longer has any children

                if (item.getExpanded()) {
                    item.setImage(getImage(FOLDER));
                }

                for (TreeItem treeItem : item.getItems()) {
                    treeItem.dispose();
                }

            } else if (item.getExpanded()) {
                // recursively refresh the file's children

                refreshItems(item.getItems(), file, index -> new TreeItem(item, NONE, index));

            } else if (nestedFileCount > 0 && item.getItemCount() == 0) {
                // the file now has some children, might not have been a folder before

                new TreeItem(item, NONE);
                item.setImage(getImage(FOLDER));
            }
        } else {
            TreeItem[] items = item.getItems();
            if (items.length > 0) {
                // the file was previously a folder but isn't anymore

                for (TreeItem treeItem : items) {
                    treeItem.dispose();
                }

                item.setImage(getIconForFile(file));
            }
        }
    }

    private void refreshItems(TreeItem[] items, File file, IntFunction<TreeItem> itemFactory) {
        File[] checkFiles = listFiles(file);
        Arrays.sort(checkFiles, (a, b) -> a.getName().compareToIgnoreCase(b.getName()));

        int itemsIndex = 0;
        int filesIndex = 0;

        while (itemsIndex < items.length && filesIndex < checkFiles.length) {
            final TreeItem treeItem = items[itemsIndex];

            final File itemFile = (File) treeItem.getData();
            final File checkFile = checkFiles[filesIndex];

            int comparison = checkFile.getName().compareTo(itemFile.getName());
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
            // add extra files at the end

            TreeItem newTreeItem = itemFactory.apply(filesIndex);
            setUpChildItem(newTreeItem, checkFiles[filesIndex]);

            filesIndex++;
        }
    }

    private List<File> getSelection() {
        return Arrays.stream(tree.getSelection())
                .map(item -> (File) item.getData())
                .collect(toList());
    }

    public void collapseAll() {
        if (tree.isEnabled()) {
            tree.setEnabled(false);
            for (TreeItem item : tree.getItems()) {
                if (((File) item.getData()).isDirectory()) {
                    item.setExpanded(false);
                    item.setImage(getImage(FOLDER));
                }
            }
            tree.setEnabled(true);
        }
    }

    private void clearTree() {
        for (TreeItem treeItem : tree.getItems()) {
            treeItem.dispose();
        }
    }

    private void addKeyListener() {
        tree.addKeyListener(KeyListener.keyPressedAdapter(e -> {
            switch (e.keyCode) {
                case 'c':
                    if (e.stateMask == SWT.CTRL && tree.getSelectionCount() > 0) {
                        copy();
                    }
                    break;
                case 'v':
                    if (e.stateMask == SWT.CTRL && tree.getSelectionCount() == 1) {
                        paste();
                    }
                    break;
                case F2:
                    if (tree.getSelectionCount() == 1) {
                        rename();
                    }
                    break;
                case DEL:
                    if (tree.getSelectionCount() > 0) {
                        delete();
                    }
                    break;
            }
        }));
    }

    private void addExpandListener() {
        tree.addListener(Expand, event -> expandTreeItem((TreeItem) event.item));
    }

    private void addCollapseListener() {
        tree.addListener(Collapse, event -> collapseTreeItem((TreeItem) event.item));
    }

    private void addDoubleClickListener() {
        tree.addListener(MouseDoubleClick, event -> {
            if (tree.getSelectionCount() == 1) {
                TreeItem item = tree.getSelection()[0];

                File file = (File) item.getData();
                if (file.isDirectory()) {
                    if (item.getExpanded()) {
                        item.setExpanded(false);
                        collapseTreeItem(item);
                    } else {
                        item.setExpanded(true);
                        expandTreeItem(item);
                    }

                } else if (FileUtils.getFileExtension(file).equals("spf")) {
                    FunctionManager.getSingleFileFunctionByName("Open").execute(file);

                } else if (FileUtils.getFileExtension(file).equals("lst")) {
                    FunctionManager.getSingleFileFunctionByName("Open .lst File").execute(file);

                } else if (PlainTextFileFilter.FILE_EXTENSIONS.contains(FileUtils.getFileExtension(file))) {
                    FunctionManager.getSingleFileFunctionByName("Open Plain Text").execute(file);
                }
            }
        });
    }

    private void expandTreeItem(TreeItem item) {
        tree.setRedraw(false);

        for (TreeItem childItem : item.getItems()) {
            childItem.dispose();
        }

        File file = (File) item.getData();

        File[] children = listFiles(file);
        Arrays.sort(children, (a, b) -> a.getName().compareToIgnoreCase(b.getName()));
        for (File child : children) {
            TreeItem childItem = new TreeItem(item, NONE);
            setUpChildItem(childItem, child);
        }

        item.setImage(getImage(OPENED_FOLDER));

        tree.setRedraw(true);
    }

    private static void setUpChildItem(TreeItem item, File file) {
        item.setText(file.getName());
        item.setData(file);

        if (file.isDirectory() && listFiles(file).length > 0) {
            new TreeItem(item, NONE);
        }

        item.setImage(getIconForFile(file));
    }

    private static void collapseTreeItem(TreeItem item) {
        item.setImage(getImage(FOLDER));
    }

    private void copy() {
        String[] fileNames = Arrays.stream(tree.getSelection())
                .map(item -> (File) item.getData())
                .map(File::getAbsolutePath)
                .toArray(String[]::new);

        clipboard.setContents(new Object[]{fileNames}, new Transfer[]{FileTransfer.getInstance()});
    }

    private void paste() {
        String[] fileNames = (String[]) clipboard.getContents(FileTransfer.getInstance());
        if (fileNames == null) {
            return;
        }

        File target = (File) tree.getSelection()[0].getData();
        if (!target.isDirectory()) {
            target = target.getParentFile();
        }

        List<String> failedFiles = FileCopy.copyTo(target.toPath(), fileNames);
        if (!failedFiles.isEmpty()) {
            Message.warning("Some files failed to copy:\n\n" + filenamesListToString(failedFiles));
        }

        refresh();
    }

    private void delete() {
        List<File> files = getSelection();

        if (!Message.question("Are you sure you want to delete the following file(s)?\n\n" + filesListToString(files))) {
            return;
        }

        List<File> failedFiles = new ArrayList<>();
        for (File file : files) {
            if (file.exists()) {
                try {
                    FileUtils.deleteFile(file.toPath());
                } catch (IOException exception) {
                    failedFiles.add(file);
                    Log.error("An error occurred while deleting file", exception);
                }
            }
        }

        if (!failedFiles.isEmpty()) {
            Message.warning("Some files failed to delete:\n\n" + filesListToString(failedFiles));
        }

        refresh();
    }

    private void rename() {
        TreeItem item = tree.getSelection()[0];

        final Composite composite = new Composite(tree, NONE);
        composite.setBackground(ColorManager.getColor(BLACK));

        final Text text = new Text(composite, NONE);

        final Consumer<String> resize = txt -> {
            GC gc = new GC(text);
            Point size = gc.textExtent(txt);
            gc.dispose();

            size = text.computeSize(size.x, DEFAULT);

            Rectangle itemBounds = item.getBounds();
            Rectangle treeClientArea = tree.getClientArea();
            treeEditor.minimumWidth = Math.min(Math.max(size.x, itemBounds.width) + 2, treeClientArea.x + treeClientArea.width - itemBounds.x);
            treeEditor.minimumHeight = size.y + 2;
            treeEditor.layout();
        };

        Listener resizeListener = e -> resize.accept(text.getText());
        tree.addListener(Resize, resizeListener);
        composite.addDisposeListener(e -> tree.removeListener(Resize, resizeListener));

        composite.addListener(Resize, event -> {
            Rectangle rectangle = composite.getClientArea();
            text.setBounds(rectangle.x + 1, rectangle.y + 1, rectangle.width - 2, rectangle.height - 2);
        });

        text.addListener(FocusOut, event -> composite.dispose());
        text.addListener(Verify, event -> resize.accept(text.getText().substring(0, event.start) + event.text + text.getText().substring(event.end)));
        text.addListener(Traverse, event -> {
            if (event.detail == TRAVERSE_RETURN || event.detail == TRAVERSE_ESCAPE) {
                if (event.detail == TRAVERSE_RETURN) {
                    Path file = ((File) item.getData()).toPath();
                    try {
                        Files.move(file, file.resolveSibling(text.getText()));
                        item.setText(text.getText());
                        refresh();

                    } catch (FileAlreadyExistsException existsException) {
                        Message.warning("File with that name already exists.");
                    } catch (IOException exception) {
                        Message.error("An error occurred while renaming file", exception);
                    }
                }

                composite.dispose();
                event.doit = false;
            }
        });

        treeEditor.setEditor(composite, item);

        text.setText(item.getText());
        if (item.getText().contains(".")) {
            text.setSelection(0, item.getText().lastIndexOf("."));
        } else {
            text.selectAll();
        }

        text.setFocus();
    }

    private static File[] listFiles(File file) {
        File[] lst = file.listFiles();
        if (lst == null) {
            throw new IllegalStateException("Couldn't list files for file " + file);
        } else {
            return lst;
        }
    }

    private class FileExplorerMenu {
        private final Menu menu;
        private final List<MenuItem> contextOptions = new ArrayList<>();

        FileExplorerMenu() {
            menu = new Menu(ComponentManager.getShell(), POP_UP | NO_RADIO_GROUP);
            tree.setMenu(menu);

            createAlwaysVisibleMenuItems();

            menu.addMenuListener(new MenuAdapter() {
                @Override
                public void menuShown(MenuEvent e) {
                    List<File> selection = getSelection();

                    contextOptions.forEach(Widget::dispose);
                    contextOptions.clear();

                    if (selection.size() == 1) {
                        final MenuItem renameItem = new MenuItem(menu, PUSH);
                        renameItem.setText("Rename\tF2");
                        renameItem.addListener(Selection, event -> rename());
                        contextOptions.add(renameItem);

                        contextOptions.add(new MenuItem(menu, SEPARATOR));

                        createMenuItems(selection.get(0),
                                (s, fileFilter) -> fileFilter.accept(s),
                                FunctionManager.getSingleFileFunctions(),
                                SingleFileFunction::execute);
                    } else if (selection.size() > 1) {
                        createMenuItems(selection,
                                (s, fileFilter) -> s.stream().allMatch(fileFilter::accept),
                                FunctionManager.getMultiFileFunctions(),
                                MultiFileFunction::execute);
                    }

                    if (contextOptions.isEmpty() || (contextOptions.size() == 2 && contextOptions.get(0).getText().startsWith("Rename"))) {
                        MenuItem item = new MenuItem(menu, PUSH);
                        item.setText("No context actions");
                        item.setEnabled(false);
                        contextOptions.add(item);
                    }
                }
            });
        }

        private void createAlwaysVisibleMenuItems() {
            final MenuItem copyItem = new MenuItem(menu, PUSH);
            copyItem.setText("Copy\tCtrl+C");
            copyItem.addListener(Selection, event -> copy());

            final MenuItem pasteItem = new MenuItem(menu, PUSH);
            pasteItem.setText("Paste\tCtrl+V");
            pasteItem.addListener(Selection, event -> paste());

            new MenuItem(menu, SEPARATOR);

            final MenuItem deleteItem = new MenuItem(menu, PUSH);
            deleteItem.setText("Delete\tDel");
            deleteItem.addListener(Selection, event -> delete());

            new MenuItem(menu, SEPARATOR);

            menu.addMenuListener(new MenuAdapter() {
                @Override
                public void menuShown(MenuEvent e) {
                    copyItem.setEnabled(tree.getSelectionCount() > 0);
                    pasteItem.setEnabled(tree.getSelectionCount() == 1 && clipboard.getContents(FileTransfer.getInstance()) != null);
                    deleteItem.setEnabled(tree.getSelectionCount() > 0);
                }
            });
        }

        private <T, S> void createMenuItems(T selection, BiPredicate<T, FileFilter> filter, List<FunctionInfo<S>> functions, BiConsumer<S, T> executor) {
            Map<String, Menu> menuGroups = new HashMap<>();

            functions.stream()
                    .filter(functionInfo -> filter.test(selection, functionInfo.getFileFilter()))
                    .filter(functionInfo -> functionInfo.getGroup().isPresent())
                    .map(functionInfo -> functionInfo.getGroup().get())
                    .distinct()
                    .sorted()
                    .forEach(group -> {
                        final MenuItem groupItem = new MenuItem(menu, CASCADE);
                        groupItem.setText(group);

                        final Menu subMenu = new Menu(menu.getShell(), DROP_DOWN | NO_RADIO_GROUP);
                        groupItem.setMenu(subMenu);

                        contextOptions.add(groupItem);
                        menuGroups.put(group, subMenu);
                    });

            if (!menuGroups.isEmpty()) {
                contextOptions.add(new MenuItem(menu, SEPARATOR));
            }

            for (FunctionInfo<S> functionInfo : functions) {
                if (filter.test(selection, functionInfo.getFileFilter())) {
                    Menu parent = functionInfo.getGroup().isPresent() ? menuGroups.get(functionInfo.getGroup().get()) : menu;

                    MenuItem item = new MenuItem(parent, PUSH);
                    item.setText(functionInfo.getName());
                    item.addListener(Selection, event -> executor.accept(functionInfo.getInstance(), selection));

                    contextOptions.add(item);
                }
            }
        }
    }
}
