package cz.cuni.mff.respefo.component;

import cz.cuni.mff.respefo.util.builders.widgets.CompositeBuilder;
import cz.cuni.mff.respefo.util.builders.widgets.LabelBuilder;
import cz.cuni.mff.respefo.util.utils.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ExpandEvent;
import org.eclipse.swt.events.ExpandListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.*;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import static cz.cuni.mff.respefo.resources.ImageManager.getIconForFile;
import static cz.cuni.mff.respefo.util.builders.FillLayoutBuilder.fillLayout;
import static cz.cuni.mff.respefo.util.builders.GridLayoutBuilder.gridLayout;
import static cz.cuni.mff.respefo.util.builders.widgets.ButtonBuilder.newButton;
import static cz.cuni.mff.respefo.util.builders.widgets.CompositeBuilder.newComposite;
import static cz.cuni.mff.respefo.util.builders.widgets.LabelBuilder.newLabel;
import static cz.cuni.mff.respefo.util.builders.widgets.TextBuilder.newText;

public class OverwriteDialog extends TitleAreaDialog {

    private static final int DEFAULT = -1;
    public static final int REPLACE = -2;
    public static final int MERGE = -3;
    public static final int RENAME = -4;
    public static final int SKIP = -5;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm");

    private final File originalFile;
    private final File replaceWith;

    private final boolean originalFileisDirectory;
    private final boolean replaceWithIsDirectory;

    private String newName = null;
    private boolean applyToAll;

    public OverwriteDialog(File file) {
        super("Replace file  \"" + file.getName() + "\"?");

        this.originalFile = file;
        this.replaceWith = null;

        originalFileisDirectory = file.isDirectory();
        replaceWithIsDirectory = false;
    }

    public OverwriteDialog(File originalFile, File replaceWith) {
        super((originalFile.isDirectory() && replaceWith.isDirectory() ? "Merge folder" : "Replace file") + " \"" + originalFile.getName() + "\"?");

        this.originalFile = originalFile;
        this.replaceWith = replaceWith;

        originalFileisDirectory = originalFile.isDirectory();
        replaceWithIsDirectory = replaceWith.isDirectory();
    }

    public String getNewName() {
        return newName;
    }

    public boolean applyToAll() {
        return applyToAll;
    }

    @Override
    protected void createButtons(Composite parent) {
        createButton(parent, DEFAULT, originalFileisDirectory && replaceWithIsDirectory ? "Merge" : "Replace", true);
        createButton(parent, SKIP, "Skip", false);
    }

    @Override
    protected void buttonPressed(int returnCode) {
        if (returnCode == DEFAULT) {
            if (newName != null) {
                returnCode = RENAME;
            } else if (originalFileisDirectory && replaceWithIsDirectory) {
                returnCode = MERGE;
            } else {
                returnCode = REPLACE;
            }
        }

        super.buttonPressed(returnCode);
    }

    @Override
    protected void createDialogArea(Composite parent) {
        if (originalFileisDirectory && replaceWithIsDirectory) {
            setMessage("Another folder with the same name already exists.\n" +
                    "Merging will ask for confirmation before replacing\n" +
                    "any files in the folder that conflict with the files\n" +
                    "being copied.", SWT.ICON_WARNING);

        } else if (originalFileisDirectory) {
            setMessage("An older directory with the same name already exists.\n" +
                    "Replacing it will overwrite its content.", SWT.ICON_WARNING);

        } else if (replaceWithIsDirectory) {
            setMessage("An older file with the same name already exists.\n" +
                    "Replacing it will overwrite its content.", SWT.ICON_WARNING);

        } else {
            setMessage("Another file with the same name already exists.\n" +
                    "Replacing it will overwrite its content.", SWT.ICON_WARNING);
        }

        final Composite topComposite = newComposite()
                .layout(gridLayout().margins(5).verticalSpacing(5))
                .gridLayoutData(GridData.FILL_BOTH)
                .build(parent);


        final Composite fileDetailsComposite = newComposite()
                .gridLayoutData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING)
                .layout(gridLayout(2, false).margins(10).spacings(10))
                .build(topComposite);

        LabelBuilder iconLabelBuilder = newLabel()
                .gridLayoutData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.VERTICAL_ALIGN_CENTER);

        CompositeBuilder fileCompositeBuilder = newComposite()
                .gridLayoutData(GridData.FILL_BOTH)
                .layout(fillLayout(SWT.VERTICAL));

        LabelBuilder fileLabel = newLabel().bold();


        iconLabelBuilder.image(getIconForFile(originalFile)).build(fileDetailsComposite);

        final Composite originalFileComposite = fileCompositeBuilder.build(fileDetailsComposite);

        fileLabel.text("Original file").build(originalFileComposite);

        newLabel().text(
                originalFileisDirectory
                        ? "Contents: " + directoryContents(originalFile) + " items"
                        : "Size: " + StringUtils.humanReadableByteCountSI(originalFile.length()))
                .build(originalFileComposite);
        newLabel().text("Last modified: " + dateFormat.format(new Date(originalFile.lastModified())))
                .build(originalFileComposite);

        if (replaceWith != null) {
            iconLabelBuilder.image(getIconForFile(replaceWith)).build(fileDetailsComposite);

            final Composite replaceWithComposite = fileCompositeBuilder.build(fileDetailsComposite);

            fileLabel.text(originalFileisDirectory && replaceWithIsDirectory ? "Merge" : "Replace" + " with")
                    .build(replaceWithComposite);

            newLabel().text(replaceWithIsDirectory
                    ? "Contents: " + directoryContents(replaceWith) + " items"
                    : "Size: " + StringUtils.humanReadableByteCountSI(replaceWith.length()))
            .build(replaceWithComposite);
            newLabel().text("Last modified: " + dateFormat.format(new Date(replaceWith.lastModified()))).build(replaceWithComposite);
        }

        final Composite expandBarComposite = newComposite()
                .layout(gridLayout().margins(5))
                .gridLayoutData(GridData.FILL_BOTH)
                .build(topComposite);

        ExpandBar bar = new ExpandBar(expandBarComposite, SWT.NONE);

        final Composite expandComposite = newComposite()
                .layout(gridLayout(2, false).margins(10))
                .build(bar);

        final Text newNameText = newText(SWT.SINGLE)
                .gridLayoutData(GridData.FILL_BOTH)
                .text(originalFile.getName())
                .build(expandComposite);

        newButton(SWT.PUSH)
                .gridLayoutData(GridData.HORIZONTAL_ALIGN_END | GridData.VERTICAL_ALIGN_FILL)
                .text("Reset")
                .onSelection(event -> {
                    newNameText.setText(originalFile.getName());
                    newNameText.setSelection(0, originalFile.getName().lastIndexOf('.'));
                    newNameText.forceFocus();
                })
        .build(expandComposite);

        ExpandItem expandItem = new ExpandItem(bar, SWT.NONE);
        expandItem.setText("Select a new name for the destination");
        expandItem.setHeight(expandComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
        expandItem.setControl(expandComposite);

        final Button applyToAllButton = newButton(SWT.CHECK)
                .gridLayoutData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_END)
                .text("Apply this action to all files and folders")
                .build(topComposite);

        bar.addExpandListener(new ExpandListener() {
            @Override
            public void itemCollapsed(ExpandEvent e) {
                ComponentManager.getDisplay().asyncExec(() -> parent.getShell().pack(true));
            }

            @Override
            public void itemExpanded(ExpandEvent e) {
                newNameText.setSelection(0, newNameText.getText().lastIndexOf('.'));
                ComponentManager.getDisplay().asyncExec(() -> {
                    parent.getShell().pack(true);
                    newNameText.forceFocus();
                });
            }
        });

        newNameText.addListener(SWT.Modify, event -> {
            Button defaultButton = getButton(DEFAULT);
            if (newNameText.getText().equals(originalFile.getName())) {
                defaultButton.setText(originalFileisDirectory && replaceWithIsDirectory ? "Merge" : "Replace");
                newName = null;
                applyToAllButton.setEnabled(true);
            } else {
                defaultButton.setText("Rename");
                newName = newNameText.getText();
                applyToAllButton.setEnabled(false);
            }
            defaultButton.pack();
        });

        applyToAllButton.addListener(SWT.Selection, event -> {
            applyToAll = applyToAllButton.getSelection();
            bar.setEnabled(!applyToAll);
        });
    }

    private String directoryContents(File file) {
        String[] lst = file.list();
        if (lst == null) {
            return "?";
        } else {
            return Integer.toString(lst.length);
        }
    }
}
