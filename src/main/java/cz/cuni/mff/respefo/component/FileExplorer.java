package cz.cuni.mff.respefo.component;

import cz.cuni.mff.respefo.resources.ImageManager;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import java.io.File;
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

    public void setRootDirectory(File file) {
        // assert file is non null and a directory?

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
}
