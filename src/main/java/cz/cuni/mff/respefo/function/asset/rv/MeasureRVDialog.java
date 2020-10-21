package cz.cuni.mff.respefo.function.asset.rv;

import cz.cuni.mff.respefo.component.TitleAreaDialog;
import cz.cuni.mff.respefo.util.FileDialogs;
import cz.cuni.mff.respefo.util.FileType;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.List;

import java.util.function.Consumer;

import static cz.cuni.mff.respefo.util.builders.ButtonBuilder.pushButton;
import static cz.cuni.mff.respefo.util.builders.CompositeBuilder.composite;
import static cz.cuni.mff.respefo.util.builders.GridDataBuilder.gridData;
import static cz.cuni.mff.respefo.util.builders.GridLayoutBuilder.gridLayout;
import static cz.cuni.mff.respefo.util.builders.LabelBuilder.label;

public class MeasureRVDialog extends TitleAreaDialog {

    private String[] measurements = {};
    private String[] corrections = {};

    public MeasureRVDialog() {
        super("Measure RV");
    }

    public String[] getMeasurements() {
        return measurements;
    }

    public String[] getCorrections() {
        return corrections;
    }

    @Override
    protected void createDialogArea(Composite parent) {
        setMessage("Measure radial velocities", SWT.ICON_INFORMATION);

        final Composite topComposite = composite(parent)
                .layout(gridLayout(2, false).margins(15).horizontalSpacing(10))
                .layoutData(gridData(GridData.FILL_BOTH).widthHint(500).heightHint(300))
                .build();


        // Measurements

        Consumer<String[]> measurementsItemsConsumer = items -> measurements = items;

        label(topComposite)
                .layoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1))
                .text("Select .lst files with measurements:")
                .build();

        final List measurementsList = new List(topComposite, SWT.BORDER | SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL);
        measurementsList.setLayoutData(new GridData(GridData.FILL_BOTH));
        measurementsList.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.keyCode == SWT.DEL) {
                    removeStlFile(measurementsList, measurementsItemsConsumer);
                } else if (e.keyCode == SWT.INSERT) {
                    addStlFile(measurementsList, measurementsItemsConsumer);
                }
            }
        });

        final Composite measurementsButtonsComposite = composite(topComposite)
                .layout(gridLayout().margins(0))
                .gridLayoutData(GridData.HORIZONTAL_ALIGN_END | GridData.VERTICAL_ALIGN_BEGINNING)
                .build();

        pushButton(measurementsButtonsComposite)
                .text("Add")
                .gridLayoutData(GridData.FILL_BOTH)
                .onSelection(event -> addStlFile(measurementsList, measurementsItemsConsumer))
                .build();

        pushButton(measurementsButtonsComposite)
                .text("Remove")
                .gridLayoutData(GridData.FILL_BOTH)
                .onSelection(event -> removeStlFile(measurementsList, measurementsItemsConsumer))
                .build();


        // Correction measurements

        Consumer<String[]> correctionsItemsConsumer = items -> corrections = items;

        label(topComposite)
                .layoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1))
                .text("Select .lst files with correction measurements:")
                .build();

        final List correctionsList = new List(topComposite, SWT.BORDER | SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL);
        correctionsList.setLayoutData(new GridData(GridData.FILL_BOTH));
        correctionsList.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.keyCode == SWT.DEL) {
                    removeStlFile(correctionsList, correctionsItemsConsumer);
                } else if (e.keyCode == SWT.INSERT) {
                    addStlFile(correctionsList, correctionsItemsConsumer);
                }
            }
        });

        final Composite correctionsButtonsComposite = composite(topComposite)
                .layout(gridLayout().margins(0))
                .gridLayoutData(GridData.HORIZONTAL_ALIGN_END | GridData.VERTICAL_ALIGN_BEGINNING)
                .build();

        pushButton(correctionsButtonsComposite)
                .text("Add")
                .gridLayoutData(GridData.FILL_BOTH)
                .onSelection(event -> addStlFile(correctionsList, correctionsItemsConsumer))
                .build();

        pushButton(correctionsButtonsComposite)
                .text("Remove")
                .gridLayoutData(GridData.FILL_BOTH)
                .onSelection(event -> removeStlFile(correctionsList, correctionsItemsConsumer))
                .build();
    }

    @Override
    protected void createButtons(Composite parent) {
        super.createButtons(parent);

        getButton(SWT.OK).setEnabled(false);
    }

    private void addStlFile(List list, Consumer<String[]> itemsConsumer) {
        String fileName = FileDialogs.openFileDialog(FileType.STL, false);
        if (fileName != null) {
            list.add(fileName);

            itemsConsumer.accept(list.getItems());
            verify();
        }
    }

    private void removeStlFile(List list, Consumer<String[]> itemsConsumer) {
        if (list.getSelectionIndex() != -1) {
            list.remove(list.getSelectionIndex());

            itemsConsumer.accept(list.getItems());
            verify();
        }
    }

    private void verify() {
        if (measurements.length == 0 && corrections.length == 0) {
            setMessage("Select at least one .stl file", SWT.ICON_WARNING);
            getButton(SWT.OK).setEnabled(false);
        } else {
            setMessage("Measure equivalent width and other spectrophotometric quantities", SWT.ICON_INFORMATION);
            getButton(SWT.OK).setEnabled(true);
        }
    }
}
