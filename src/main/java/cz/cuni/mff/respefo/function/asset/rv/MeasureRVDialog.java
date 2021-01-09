package cz.cuni.mff.respefo.function.asset.rv;

import cz.cuni.mff.respefo.component.TitleAreaDialog;
import cz.cuni.mff.respefo.util.FileDialogs;
import cz.cuni.mff.respefo.util.FileType;
import cz.cuni.mff.respefo.util.builders.widgets.ButtonBuilder;
import cz.cuni.mff.respefo.util.builders.widgets.CompositeBuilder;
import cz.cuni.mff.respefo.util.builders.widgets.LabelBuilder;
import cz.cuni.mff.respefo.util.builders.widgets.ListBuilder;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.List;

import java.util.function.Consumer;

import static cz.cuni.mff.respefo.util.builders.GridDataBuilder.gridData;
import static cz.cuni.mff.respefo.util.builders.GridLayoutBuilder.gridLayout;
import static cz.cuni.mff.respefo.util.builders.widgets.CompositeBuilder.newComposite;

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

        final Composite topComposite = newComposite()
                .layout(gridLayout(2, false).margins(15).horizontalSpacing(10))
                .layoutData(gridData(GridData.FILL_BOTH).widthHint(500).heightHint(300).build())
                .build(parent);

        LabelBuilder labelBuilder = LabelBuilder.newLabel().gridLayoutData(SWT.FILL, SWT.TOP, true, false, 2, 1);
        ListBuilder listBuilder = ListBuilder.newList(SWT.BORDER | SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL).gridLayoutData(GridData.FILL_BOTH);
        CompositeBuilder buttonsCompositeBuilder = CompositeBuilder.newComposite()
                .layout(gridLayout().margins(0))
                .gridLayoutData(GridData.HORIZONTAL_ALIGN_END | GridData.VERTICAL_ALIGN_BEGINNING);
        ButtonBuilder addButtonBuilder = ButtonBuilder.newButton(SWT.PUSH).gridLayoutData(GridData.FILL_BOTH).text("Add");
        ButtonBuilder removeButtonBuilder = ButtonBuilder.newButton(SWT.PUSH).gridLayoutData(GridData.FILL_BOTH).text("Remove");

        // Measurements

        Consumer<String[]> measurementsItemsConsumer = items -> measurements = items;

        labelBuilder.text("Select .lst files with measurements:").build(topComposite);

        final List measurementsList = listBuilder.listener(SWT.KeyDown, e -> {
            if (e.keyCode == SWT.DEL) {
                removeStlFile((List) e.widget, measurementsItemsConsumer);
            } else if (e.keyCode == SWT.INSERT) {
                addStlFile((List) e.widget, measurementsItemsConsumer);
            }
        }).build(topComposite);

        final Composite measurementsButtonsComposite = buttonsCompositeBuilder.build(topComposite);
        addButtonBuilder
                .onSelection(event -> addStlFile(measurementsList, measurementsItemsConsumer))
                .build(measurementsButtonsComposite);
        removeButtonBuilder
                .onSelection(event -> removeStlFile(measurementsList, measurementsItemsConsumer))
                .build(measurementsButtonsComposite);

        // Correction measurements

        Consumer<String[]> correctionsItemsConsumer = items -> corrections = items;

        labelBuilder.text("Select .lst files with correction measurements:").build(topComposite);

        final List correctionsList = listBuilder.listener(SWT.KeyDown, e -> {
            if (e.keyCode == SWT.DEL) {
                removeStlFile((List) e.widget, correctionsItemsConsumer);
            } else if (e.keyCode == SWT.INSERT) {
                addStlFile((List) e.widget, correctionsItemsConsumer);
            }
        }).build(topComposite);

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
