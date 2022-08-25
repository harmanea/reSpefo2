package cz.cuni.mff.respefo.function.port;

import cz.cuni.mff.respefo.dialog.TitleAreaDialog;
import cz.cuni.mff.respefo.spectrum.port.FileFormat;
import cz.cuni.mff.respefo.spectrum.port.ImportFileFormat;
import cz.cuni.mff.respefo.util.Async;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ExpandEvent;
import org.eclipse.swt.events.ExpandListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.*;

import java.util.List;
import java.util.Optional;

import static cz.cuni.mff.respefo.util.layout.GridDataBuilder.gridData;
import static cz.cuni.mff.respefo.util.layout.GridLayoutBuilder.gridLayout;
import static cz.cuni.mff.respefo.util.widget.ButtonBuilder.newButton;
import static cz.cuni.mff.respefo.util.widget.CompositeBuilder.newComposite;
import static cz.cuni.mff.respefo.util.widget.LabelBuilder.newLabel;
import static cz.cuni.mff.respefo.util.widget.TextBuilder.newText;

public class ImportDialog extends TitleAreaDialog {
    private final List<ImportFileFormat> fileFormats;
    private final Options options;

    public Options getOptions() {
        return options;
    }

    protected ImportDialog(List<ImportFileFormat> fileFormats) {
        super("Import");

        this.fileFormats = fileFormats;
        options = new Options();
    }

    @Override
    protected void createDialogArea(Composite parent) {
        setMessage("Select import format and options.", SWT.ICON_INFORMATION);

        final ScrolledComposite scrolledComposite = new ScrolledComposite(parent, SWT.V_SCROLL);
        scrolledComposite.setLayoutData(gridData(GridData.FILL_BOTH).build());
        scrolledComposite.setLayout(new FillLayout(SWT.VERTICAL));

        Composite composite = createScrollableContent(scrolledComposite);

        scrolledComposite.setContent(composite);
        composite.setSize(composite.computeSize(500, SWT.DEFAULT));
    }

    private Composite createScrollableContent(ScrolledComposite parent) {
        final Composite composite = newComposite()
                .layout(gridLayout(2, false).margins(15).spacings(15))
                .build(parent);

        newLabel()
                .text("Format:")
                .gridLayoutData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.VERTICAL_ALIGN_CENTER)
                .build(composite);

        final Combo formatSelector = new Combo(composite, SWT.DROP_DOWN | SWT.READ_ONLY);
        formatSelector.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.VERTICAL_ALIGN_CENTER));
        for (ImportFileFormat format : fileFormats) {
            formatSelector.add(format.name());
            formatSelector.setData(format.name(), format);
            if (format.isDefault()) {
                formatSelector.select(formatSelector.getItemCount() - 1);
            }
        }

        final StyledText descriptionText = new StyledText(composite, SWT.MULTI | SWT.READ_ONLY | SWT.WRAP | SWT.V_SCROLL | SWT.BORDER);
        descriptionText.setText(options.format.description());
        descriptionText.setLayoutData(gridData(GridData.FILL_BOTH).horizontalSpan(2).heightHint(100).build());
        descriptionText.setMargins(10, 10, 10, 10);

        formatSelector.addListener(SWT.Selection, event -> {
            int index = formatSelector.getSelectionIndex();
            if (index >= 0) {
                String name = formatSelector.getItem(index);
                ImportFileFormat format = (ImportFileFormat) formatSelector.getData(name);
                descriptionText.setText(format.description());
                descriptionText.setSelection(0);
                options.format = format;
            }
        });

        ExpandBar bar = new ExpandBar(composite, SWT.NONE);
        bar.setLayoutData(gridData(GridData.FILL_BOTH).horizontalSpan(2).build());
        bar.addExpandListener(new ExpandListener() {
            private void update() {
                Async.exec(() -> composite.setSize(composite.computeSize(500, SWT.DEFAULT)));
            }

            @Override
            public void itemCollapsed(ExpandEvent e) {
                update();
            }

            @Override
            public void itemExpanded(ExpandEvent e) {
                update();
            }
        });

        final Composite expandComposite = newComposite()
                .layout(gridLayout(2, true).margins(10))
                .build(bar);

        Button nanButton = newButton(SWT.CHECK)
                .selection(options.nanReplacement.isPresent())
                .gridLayoutData(GridData.FILL_BOTH)
                .text("Replace NaN values with:")
                .build(expandComposite);

        Text nanText = newText(SWT.SINGLE)
                .gridLayoutData(GridData.FILL_BOTH)
                .text(options.nanReplacement.isPresent() ? Double.toString(options.nanReplacement.get()) : "")
                .onModify(event -> {
                    if (nanButton.getSelection()) {
                        options.nanReplacement = Optional.ofNullable(parseText((Text) event.widget));
                    }
                })
                .build(expandComposite);

        nanButton.addListener(SWT.Selection, event -> {
            if (nanButton.getSelection()) {
                nanText.setEnabled(true);
                options.nanReplacement = Optional.ofNullable(parseText(nanText));
            } else {
                nanText.setEnabled(false);
                options.nanReplacement = Optional.empty();
            }
        });

        Button rvButton = newButton(SWT.CHECK)
                .selection(options.nanReplacement.isPresent())
                .gridLayoutData(GridData.FILL_BOTH)
                .text("Default RV correction value:")
                .build(expandComposite);

        Text rvText = newText(SWT.SINGLE)
                .gridLayoutData(GridData.FILL_BOTH)
                .text(options.defaultRvCorrection.isPresent() ? Double.toString(options.defaultRvCorrection.get()) : "")
                .onModify(event -> {
                    if (nanButton.getSelection()) {
                        options.defaultRvCorrection = Optional.ofNullable(parseText((Text) event.widget));
                    }
                })
                .build(expandComposite);

        rvButton.addListener(SWT.Selection, event -> {
            if (rvButton.getSelection()) {
                rvText.setEnabled(true);
                options.defaultRvCorrection = Optional.ofNullable(parseText(rvText));
            } else {
                rvText.setEnabled(false);
                options.defaultRvCorrection = Optional.empty();
            }
        });

        ExpandItem expandItem = new ExpandItem(bar, SWT.NONE);
        expandItem.setText("Advanced options");
        expandItem.setHeight(expandComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
        expandItem.setControl(expandComposite);

        return composite;
    }

    private Double parseText(Text text) {
        try {
            return Double.parseDouble(text.getText());
        } catch (NumberFormatException | NullPointerException exception) {
            return null;
        }
    }

    public class Options {
        public ImportFileFormat format;
        public Optional<Double> nanReplacement;
        public Optional<Double> defaultRvCorrection;
        // TODO: Add options for .lst files

        public Options() {
            format = fileFormats.stream()
                    .filter(FileFormat::isDefault)
                    .findFirst()
                    .orElse(fileFormats.get(0));
            nanReplacement = Optional.of(0.0);
            defaultRvCorrection = Optional.of(0.0);
        }
    }
}
