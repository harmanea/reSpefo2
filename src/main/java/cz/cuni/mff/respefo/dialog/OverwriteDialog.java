package cz.cuni.mff.respefo.dialog;

import cz.cuni.mff.respefo.util.Async;
import cz.cuni.mff.respefo.util.utils.StringUtils;
import cz.cuni.mff.respefo.util.widget.CompositeBuilder;
import cz.cuni.mff.respefo.util.widget.LabelBuilder;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ExpandEvent;
import org.eclipse.swt.events.ExpandListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.stream.Stream;

import static cz.cuni.mff.respefo.resources.ImageManager.getIconForFile;
import static cz.cuni.mff.respefo.util.layout.FillLayoutBuilder.fillLayout;
import static cz.cuni.mff.respefo.util.layout.GridLayoutBuilder.gridLayout;
import static cz.cuni.mff.respefo.util.widget.ButtonBuilder.newCheckButton;
import static cz.cuni.mff.respefo.util.widget.ButtonBuilder.newPushButton;
import static cz.cuni.mff.respefo.util.widget.CompositeBuilder.newComposite;
import static cz.cuni.mff.respefo.util.widget.LabelBuilder.newLabel;
import static cz.cuni.mff.respefo.util.widget.TextBuilder.newText;

public class OverwriteDialog extends TitleAreaDialog {

    private static final int DEFAULT = -1;
    public static final int REPLACE = -2;
    public static final int MERGE = -3;
    public static final int RENAME = -4;
    public static final int SKIP = -5;

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd-MM-yyyy HH:mm");

    private final Path original;
    private final Path replaceWith;

    private final boolean originalFileIsDirectory;
    private final boolean replaceWithIsDirectory;

    private String newName = null;
    private boolean applyToAll;

    public OverwriteDialog(Path path) {
        super("Replace file  \"" + path.getFileName().toString() + "\"?");

        this.original = path;
        this.replaceWith = null;

        originalFileIsDirectory = Files.isDirectory(path);
        replaceWithIsDirectory = false;
    }

    public OverwriteDialog(Path original, Path replaceWith) {
        super((Files.isDirectory(original) && Files.isDirectory(replaceWith) ? "Merge folder" : "Replace file") + " \"" + original.getFileName().toString() + "\"?");

        this.original = original;
        this.replaceWith = replaceWith;

        originalFileIsDirectory = Files.isDirectory(original);
        replaceWithIsDirectory = Files.isDirectory(replaceWith);
    }

    public String getNewName() {
        return newName;
    }

    public boolean applyToAll() {
        return applyToAll;
    }

    @Override
    protected void createButtons(Composite parent) {
        createButton(parent, DEFAULT, originalFileIsDirectory && replaceWithIsDirectory ? "Merge" : "Replace", true);
        createButton(parent, SKIP, "Skip", false);
    }

    @Override
    protected void buttonPressed(int returnCode) {
        if (returnCode == DEFAULT) {
            if (newName != null) {
                returnCode = RENAME;
            } else if (originalFileIsDirectory && replaceWithIsDirectory) {
                returnCode = MERGE;
            } else {
                returnCode = REPLACE;
            }
        }

        super.buttonPressed(returnCode);
    }

    @Override
    protected void createDialogArea(Composite parent) {
        if (originalFileIsDirectory && replaceWithIsDirectory) {
            setMessage("Another folder with the same name already exists.\n" +
                    "Merging will ask for confirmation before replacing\n" +
                    "any files in the folder that conflict with the files\n" +
                    "being copied.", SWT.ICON_WARNING);

        } else if (originalFileIsDirectory) {
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


        iconLabelBuilder.image(getIconForFile(original)).build(fileDetailsComposite);

        final Composite originalFileComposite = fileCompositeBuilder.build(fileDetailsComposite);

        fileLabel.text("Original file").build(originalFileComposite);

        newLabel().text(
                originalFileIsDirectory
                        ? "Contents: " + directoryContents(original) + " items"
                        : "Size: " + fileSize(original))
                .build(originalFileComposite);
        newLabel().text("Last modified: " + lastModified(original))
                .build(originalFileComposite);

        if (replaceWith != null) {
            iconLabelBuilder.image(getIconForFile(replaceWith)).build(fileDetailsComposite);

            final Composite replaceWithComposite = fileCompositeBuilder.build(fileDetailsComposite);

            fileLabel.text(originalFileIsDirectory && replaceWithIsDirectory ? "Merge" : "Replace" + " with")
                    .build(replaceWithComposite);

            newLabel().text(replaceWithIsDirectory
                    ? "Contents: " + directoryContents(replaceWith) + " items"
                    : "Size: " + fileSize(replaceWith))
            .build(replaceWithComposite);
            newLabel().text("Last modified: " + lastModified(replaceWith)).build(replaceWithComposite);
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
                .text(original.getFileName().toString())
                .build(expandComposite);

        newPushButton()
                .gridLayoutData(GridData.HORIZONTAL_ALIGN_END | GridData.VERTICAL_ALIGN_FILL)
                .text("Reset")
                .onSelection(event -> {
                    newNameText.setText(original.getFileName().toString());
                    newNameText.setSelection(0, original.getFileName().toString().lastIndexOf('.'));
                    newNameText.forceFocus();
                })
        .build(expandComposite);

        ExpandItem expandItem = new ExpandItem(bar, SWT.NONE);
        expandItem.setText("Select a new name for the destination");
        expandItem.setHeight(expandComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
        expandItem.setControl(expandComposite);

        final Button applyToAllButton = newCheckButton()
                .gridLayoutData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_END)
                .text("Apply this action to all files and folders")
                .build(topComposite);

        bar.addExpandListener(new ExpandListener() {
            @Override
            public void itemCollapsed(ExpandEvent e) {
                Async.exec(() -> parent.getShell().pack(true));
            }

            @Override
            public void itemExpanded(ExpandEvent e) {
                newNameText.setSelection(0, newNameText.getText().lastIndexOf('.'));
                Async.exec(() -> {
                    parent.getShell().pack(true);
                    newNameText.forceFocus();
                });
            }
        });

        newNameText.addListener(SWT.Modify, event -> {
            Button defaultButton = getButton(DEFAULT);
            if (newNameText.getText().equals(original.getFileName().toString())) {
                defaultButton.setText(originalFileIsDirectory && replaceWithIsDirectory ? "Merge" : "Replace");
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

    private static String directoryContents(Path path) {
        try (Stream<Path> lst = Files.list(path)) {
           return Long.toString(lst.count());

        } catch (IOException e) {
            return "?";
        }
    }

    private static String fileSize(Path path) {
        try {
            long bytes = Files.size(path);
            return StringUtils.humanReadableByteCountSI(bytes);

        } catch (IOException e) {
            return "?";
        }
    }

    private static String lastModified(Path path) {
        try {
            FileTime lastModified = Files.getLastModifiedTime(path);
            return DATE_FORMAT.format(Date.from(lastModified.toInstant()));

        } catch (IOException e) {
            return "?";
        }
    }
}
