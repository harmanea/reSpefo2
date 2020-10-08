package cz.cuni.mff.respefo.component;

import cz.cuni.mff.respefo.SpefoException;
import cz.cuni.mff.respefo.format.FormatManager;
import cz.cuni.mff.respefo.function.FunctionInfo;
import cz.cuni.mff.respefo.function.FunctionManager;
import cz.cuni.mff.respefo.function.MultiFileFunction;
import cz.cuni.mff.respefo.function.SingleFileFunction;
import cz.cuni.mff.respefo.logging.Log;
import cz.cuni.mff.respefo.resources.ColorManager;
import cz.cuni.mff.respefo.resources.ImageResource;
import cz.cuni.mff.respefo.util.Message;
import cz.cuni.mff.respefo.util.utils.FileDialogs;
import cz.cuni.mff.respefo.util.utils.FileUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TreeEditor;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.*;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.stream.Collectors;

import static cz.cuni.mff.respefo.resources.ColorResource.BLACK;
import static cz.cuni.mff.respefo.resources.ImageManager.getImage;
import static cz.cuni.mff.respefo.resources.ImageResource.*;
import static cz.cuni.mff.respefo.util.utils.FileUtils.getFileExtension;
import static java.util.stream.Collectors.toList;
import static org.eclipse.swt.SWT.*;

public class FileExplorer {
    private final Tree tree;
    private final TreeEditor treeEditor;

    private final Clipboard clipboard;

    private File rootDirectory;

    public FileExplorer(Composite parent) {
        tree = new Tree(parent, BORDER | MULTI | V_SCROLL);

        treeEditor = new TreeEditor(tree);
        treeEditor.horizontalAlignment = LEFT;

        addExpandListener();
        addCollapseListener();
        addDoubleClickListener();
        addKeyListener();

        clipboard = new Clipboard(ComponentManager.getDisplay());

        new FileExplorerMenu(tree);
    }

    public void setLayoutData(Object layoutData) {
        tree.setLayoutData(layoutData);
    }

    public void setRootDirectory(File file) throws SpefoException {
        if (file == null) {
            throw new SpefoException("File is null");
        } else if (!file.isDirectory()) {
            throw new SpefoException("File is not a directory");
        } else if (!file.exists()) {
            throw new SpefoException("File doesn't exist");
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

    public void changeDirectory() {
        DirectoryDialog dialog = new DirectoryDialog(tree.getShell());

        dialog.setText("Choose directory");
        dialog.setFilterPath(getRootDirectory().getPath());

        String directoryName = dialog.open();

        if (directoryName != null) {
            try {
                setRootDirectory(new File(directoryName));
                FileDialogs.setFilterPath(directoryName);
            } catch (Exception exception) {
                Message.error("Couldn't change directory.", exception);
            }
        }
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

    private void addKeyListener() {
        tree.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
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
            }
        });
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
            } else if (FormatManager.getImportableFileExtensions().contains(fileExtension)) {
                return IMPORTABLE_FILE;
            } else if (fileExtension.equals("stl") || fileExtension.equals("lst")) {
                return SUPPORT_FILE;
            } else {
                return FILE; // TODO: add more custom icons
            }
        }
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

        if (fileNames != null) {
            for (String fileName : fileNames){
                try {
                    Path source = Paths.get(fileName);
                    File target = (File) tree.getSelection()[0].getData();
                    if (!target.isDirectory()) {
                        target = target.getParentFile();
                    }

                    copyFile(source, target.toPath());

                } catch (IOException exception) {
                    Log.error("An error occurred while pasting", exception);
                }
            }

            refresh();
        }
    }

    private void copyFile(Path source, Path target, CopyOption... options) throws IOException {
        if (Files.isDirectory(source)) {
            Files.createDirectories(target.resolve(source.getFileName()));

            Files.walkFileTree(source, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    Files.createDirectories(target.resolve(source.getParent().relativize(dir)));
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.copy(file, target.resolve(source.getParent().relativize(file)), options);
                    return FileVisitResult.CONTINUE;
                }
            });
        } else {
            Files.copy(source, target.resolve(source.getFileName()), options);
        }
    }

    private void delete() {
        List<File> files = getSelection();

        if (!Message.question("Are you sure you want to delete the following file(s)?\n\n" + filesListToString(files))) {
            return;
        }

        for (File file : files) {
            if (file.exists()) {
                try {
                    deleteFile(file);
                } catch (IOException exception) {
                    Log.error("An error occurred while deleting file", exception);
                }
            }
        }

        refresh();
    }

    private String filesListToString(List<File> files) {
        return (files.size() > 5 ? files.subList(0, 5)  : files)
                .stream().map(file -> FileUtils.getRelativePath(file).toString()).collect(Collectors.joining("\n"))
                + (files.size() > 5 ? "\n\nand " + (files.size() - 5) + " more"  : "");
    }

    private void deleteFile(File file) throws IOException {
        if (file.isDirectory()) {
            Files.walkFileTree(file.toPath(), new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });

        } else {
            Files.delete(file.toPath());
        }
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

    public class FileExplorerMenu {
        private final Menu menu;

        private final List<MenuItem> contextOptions = new ArrayList<>();

        FileExplorerMenu(Tree tree) {
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

                    if (contextOptions.isEmpty()) {
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
