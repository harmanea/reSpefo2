package cz.cuni.mff.respefo.function.rv;

import cz.cuni.mff.respefo.dialog.TitleAreaDialog;
import cz.cuni.mff.respefo.util.FileDialogs;
import cz.cuni.mff.respefo.util.FileType;
import cz.cuni.mff.respefo.util.widget.ButtonBuilder;
import cz.cuni.mff.respefo.util.widget.CompositeBuilder;
import cz.cuni.mff.respefo.util.widget.LabelBuilder;
import cz.cuni.mff.respefo.util.widget.ListBuilder;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.List;

import java.util.function.Consumer;

import static cz.cuni.mff.respefo.util.layout.GridDataBuilder.gridData;
import static cz.cuni.mff.respefo.util.layout.GridLayoutBuilder.gridLayout;
import static cz.cuni.mff.respefo.util.widget.CompositeBuilder.newComposite;

public class MeasureRVDialog extends TitleAreaDialog {

    private static String[] previousMeasurementFileNames = {};
    private static String[] previousCorrectionFileNames = {};

    private String[] measurementFileNames;
    private String[] correctionFileNames;

    public MeasureRVDialog() {
        super("Measure RV");

        measurementFileNames = previousMeasurementFileNames;
        correctionFileNames = previousCorrectionFileNames;
    }

    public String[] getMeasurementFileNames() {
        return measurementFileNames;
    }

    public String[] getCorrectionFileNames() {
        return correctionFileNames;
    }

    @Override
    protected void createDialogArea(Composite parent) {
        setMessage("Measure radial velocities", SWT.ICON_INFORMATION);

        final Composite topComposite = newComposite()
                .layout(gridLayout(2, false).margins(15).horizontalSpacing(10))
                .layoutData(gridData(GridData.FILL_BOTH).widthHint(500).heightHint(300))
                .build(parent);

        LabelBuilder labelBuilder = LabelBuilder.newLabel().gridLayoutData(SWT.FILL, SWT.TOP, true, false, 2, 1);
        ListBuilder listBuilder = ListBuilder.newList(SWT.BORDER | SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL).gridLayoutData(GridData.FILL_BOTH);
        CompositeBuilder buttonsCompositeBuilder = CompositeBuilder.newComposite()
                .layout(gridLayout().margins(0))
                .gridLayoutData(GridData.HORIZONTAL_ALIGN_END | GridData.VERTICAL_ALIGN_BEGINNING);
        ButtonBuilder addButtonBuilder = ButtonBuilder.newPushButton().gridLayoutData(GridData.FILL_BOTH).text("Add");
        ButtonBuilder removeButtonBuilder = ButtonBuilder.newPushButton().gridLayoutData(GridData.FILL_BOTH).text("Remove");

        // Measurements

        Consumer<String[]> measurementsItemsConsumer = items -> measurementFileNames = items;

        labelBuilder.text("Select .stl files with measurements:").build(topComposite);

        final List measurementsList = listBuilder.listener(SWT.KeyDown, e -> {
            if (e.keyCode == SWT.DEL) {
                removeStlFile((List) e.widget, measurementsItemsConsumer);
            } else if (e.keyCode == SWT.INSERT || e.keyCode == SWT.HELP  || (e.keyCode == 'i' && e.stateMask == SWT.COMMAND)) {
                addStlFile((List) e.widget, measurementsItemsConsumer);
            }
        }).build(topComposite);
        measurementsList.setItems(measurementFileNames);

        final Composite measurementsButtonsComposite = buttonsCompositeBuilder.build(topComposite);
        addButtonBuilder
                .onSelection(event -> addStlFile(measurementsList, measurementsItemsConsumer))
                .build(measurementsButtonsComposite);
        removeButtonBuilder
                .onSelection(event -> removeStlFile(measurementsList, measurementsItemsConsumer))
                .build(measurementsButtonsComposite);

        // Correction measurements

        Consumer<String[]> correctionsItemsConsumer = items -> correctionFileNames = items;

        labelBuilder.text("Select .stl files with correction measurements:").build(topComposite);

        final List correctionsList = listBuilder.listener(SWT.KeyDown, e -> {
            if (e.keyCode == SWT.DEL) {
                removeStlFile((List) e.widget, correctionsItemsConsumer);
            } else if (e.keyCode == SWT.INSERT || e.keyCode == SWT.HELP || (e.keyCode == 'i' && e.stateMask == SWT.COMMAND)) {
                addStlFile((List) e.widget, correctionsItemsConsumer);
            }
        }).build(topComposite);
        correctionsList.setItems(correctionFileNames);

        final Composite correctionsButtonsComposite = buttonsCompositeBuilder.build(topComposite);
        addButtonBuilder
                .onSelection(event -> addStlFile(correctionsList, correctionsItemsConsumer))
                .build(correctionsButtonsComposite);
        removeButtonBuilder
                .onSelection(event -> removeStlFile(correctionsList, correctionsItemsConsumer))
                .build(correctionsButtonsComposite);
    }

    @Override
    protected void createButtons(Composite parent) {
        super.createButtons(parent);

        getButton(SWT.OK).setEnabled(measurementFileNames.length + correctionFileNames.length > 0);
    }

    private void addStlFile(List list, Consumer<String[]> itemsConsumer) {
        FileDialogs.openFileDialog(FileType.STL, false)
                .ifPresent(fileName -> {
                    list.add(fileName);
                    itemsConsumer.accept(list.getItems());
                    verify();
                });
    }

    private void removeStlFile(List list, Consumer<String[]> itemsConsumer) {
        if (list.getSelectionIndex() != -1) {
            list.remove(list.getSelectionIndex());

            itemsConsumer.accept(list.getItems());
            verify();
        }
    }

    private void verify() {
        if (measurementFileNames.length == 0 && correctionFileNames.length == 0) {
            setMessage("Select at least one .stl file", SWT.ICON_WARNING);
            getButton(SWT.OK).setEnabled(false);
        } else {
            setMessage("Measure radial velocities", SWT.ICON_INFORMATION);
            getButton(SWT.OK).setEnabled(true);
        }
    }

    @Override
    protected void buttonPressed(int returnCode) {
        if (returnCode == SWT.OK) {
            synchronized (this) {
                previousCorrectionFileNames = correctionFileNames;
                previousMeasurementFileNames = measurementFileNames;
            }
        }

        super.buttonPressed(returnCode);
    }
}
