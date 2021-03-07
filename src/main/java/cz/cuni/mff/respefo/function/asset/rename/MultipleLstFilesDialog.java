package cz.cuni.mff.respefo.function.asset.rename;

import cz.cuni.mff.respefo.component.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

import java.io.File;
import java.util.List;

import static cz.cuni.mff.respefo.util.builders.GridDataBuilder.gridData;
import static cz.cuni.mff.respefo.util.builders.GridLayoutBuilder.gridLayout;
import static cz.cuni.mff.respefo.util.builders.widgets.CompositeBuilder.newComposite;
import static cz.cuni.mff.respefo.util.builders.widgets.ListBuilder.newList;


public class MultipleLstFilesDialog extends TitleAreaDialog {

    private final List<File> lstFiles;
    private File selectedFile;

    public MultipleLstFilesDialog(List<File> lstFiles) {
        super("Select .lst file");

        this.lstFiles = lstFiles;
        selectedFile = lstFiles.get(0);
    }

    public File getSelectedFile() {
        return selectedFile;
    }

    @Override
    protected void createDialogArea(Composite parent) {
        setMessage("Multiple .lst files found, select which one to use.", SWT.ICON_INFORMATION);

        final Composite composite = newComposite()
                .layout(gridLayout().margins(15))
                .layoutData(gridData(GridData.FILL_BOTH).widthHint(400).build())
                .build(parent);

        final org.eclipse.swt.widgets.List list = newList(SWT.BORDER | SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL)
                .gridLayoutData(GridData.FILL_BOTH)
                .selection(0)
                .onSelection(event -> {
                    int index = ((org.eclipse.swt.widgets.List) event.widget).getSelectionIndex();
                    if (index >= 0) {
                        selectedFile = lstFiles.get(index);
                        setMessage("Multiple .lst files found, select which one to use.", SWT.ICON_INFORMATION);
                        getButton(SWT.OK).setEnabled(true);
                    } else {
                        setMessage("One file must be selected.", SWT.ICON_WARNING);
                        getButton(SWT.OK).setEnabled(false);
                    }
                })
                .build(composite);

        for (File lstFile : lstFiles) {
            list.add(lstFile.getName());
        }
    }
}
