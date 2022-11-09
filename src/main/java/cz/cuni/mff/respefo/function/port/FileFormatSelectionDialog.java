package cz.cuni.mff.respefo.function.port;

import cz.cuni.mff.respefo.dialog.SpefoDialog;
import cz.cuni.mff.respefo.spectrum.port.FileFormat;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import java.util.Comparator;
import java.util.List;

import static cz.cuni.mff.respefo.util.layout.GridDataBuilder.gridData;
import static cz.cuni.mff.respefo.util.layout.GridLayoutBuilder.gridLayout;
import static cz.cuni.mff.respefo.util.widget.CompositeBuilder.newComposite;
import static cz.cuni.mff.respefo.util.widget.LabelBuilder.newLabel;

public class FileFormatSelectionDialog<T extends FileFormat> extends SpefoDialog {
    private int selectionIndex;
    private final List<T> fileFormats;
    private final String type;

    public FileFormatSelectionDialog(List<T> fileFormats, String type) {
        super(type);
        this.selectionIndex = -1;
        this.fileFormats = fileFormats;
        this.type = type;

        fileFormats.sort(Comparator.comparing(FileFormat::name));
    }

    public T getFileFormat() {
        return fileFormats.get(selectionIndex);
    }

    @Override
    protected void createDialogArea(Composite parent) {
        final Composite composite = newComposite()
                .layout(gridLayout(2, false).margins(15).spacings(15))
                .layoutData(gridData(GridData.FILL_BOTH).widthHint(450))
                .build(parent);

        newLabel()
                .text("Select an " + type.toLowerCase() + " format:")
                .gridLayoutData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.VERTICAL_ALIGN_CENTER)
                .build(composite);

        final Combo formatSelector = new Combo(composite, SWT.DROP_DOWN | SWT.READ_ONLY);
        formatSelector.setLayoutData(new GridData(SWT.END, SWT.CENTER, false, true));
        String[] items = fileFormats.stream().map(FileFormat::name).toArray(String[]::new);
        formatSelector.setItems(items);
        for (int i = 0; i < fileFormats.size(); i++) {
            if (fileFormats.get(i).isDefault()) {
                formatSelector.select(i);
                selectionIndex = i;
                break;
            }
        }

        final Label descriptionLabel = newLabel(SWT.WRAP)
                .text(selectionIndex >= 0 ? getFileFormat().description() : "")
                .gridLayoutData(SWT.CENTER, SWT.BOTTOM, true, true, 2, 1)
                .build(composite);

        formatSelector.addListener(SWT.Selection, event -> {
            selectionIndex = formatSelector.getSelectionIndex();
            if (selectionIndex >= 0) {
                descriptionLabel.setText(getFileFormat().description());
                descriptionLabel.getShell().pack();
                descriptionLabel.getShell().layout();
            }
        });
    }
}
