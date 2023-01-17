package cz.cuni.mff.respefo.function.port;

import cz.cuni.mff.respefo.dialog.TitleAreaDialog;
import cz.cuni.mff.respefo.spectrum.port.ExportFileFormat;
import cz.cuni.mff.respefo.spectrum.port.FileFormat;
import cz.cuni.mff.respefo.util.Async;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ExpandEvent;
import org.eclipse.swt.events.ExpandListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ExpandBar;
import org.eclipse.swt.widgets.ExpandItem;

import java.util.List;

import static cz.cuni.mff.respefo.util.layout.GridDataBuilder.gridData;
import static cz.cuni.mff.respefo.util.layout.GridLayoutBuilder.gridLayout;
import static cz.cuni.mff.respefo.util.widget.ButtonBuilder.newCheckButton;
import static cz.cuni.mff.respefo.util.widget.CompositeBuilder.newComposite;
import static cz.cuni.mff.respefo.util.widget.LabelBuilder.newLabel;

public class ExportDialog extends TitleAreaDialog {
    private final List<ExportFileFormat> fileFormats;
    private final Options options;

    public Options getOptions() {
        return options;
    }

    protected ExportDialog(List<ExportFileFormat> fileFormats) {
        super("Export");

        this.fileFormats = fileFormats;
        options = new Options();
    }

    @Override
    protected void createDialogArea(Composite parent) {
        setMessage("Select export format and options.", SWT.ICON_INFORMATION);

        final ScrolledComposite scrolledComposite = new ScrolledComposite(parent, SWT.V_SCROLL);
        scrolledComposite.setLayoutData(gridData(GridData.FILL_BOTH).build());
        scrolledComposite.setLayout(new FillLayout(SWT.VERTICAL));

        final Composite composite = newComposite()
                .layout(gridLayout(2, false).margins(15).spacings(15))
                .build(scrolledComposite);

        newLabel()
                .text("Format:")
                .gridLayoutData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.VERTICAL_ALIGN_CENTER)
                .build(composite);

        final Combo formatSelector = new Combo(composite, SWT.DROP_DOWN | SWT.READ_ONLY);
        formatSelector.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.VERTICAL_ALIGN_CENTER));
        for (ExportFileFormat format : fileFormats) {
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
                ExportFileFormat format = (ExportFileFormat) formatSelector.getData(name);
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
                .layout(gridLayout().margins(10))
                .build(bar);

        newCheckButton()
                .selection(options.applyZeroPointRvCorrection)
                .gridLayoutData(GridData.FILL_BOTH)
                .text("Apply zero point RV correction from measured RV of telluric lines")
                .onSelectedValue(value -> options.applyZeroPointRvCorrection = value)
                .build(expandComposite);

        ExpandItem expandItem = new ExpandItem(bar, SWT.NONE);
        expandItem.setText("Advanced options");
        expandItem.setHeight(expandComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
        expandItem.setControl(expandComposite);

        scrolledComposite.setContent(composite);
        composite.setSize(composite.computeSize(500, SWT.DEFAULT));
    }

    public class Options {
        public ExportFileFormat format;
        public boolean applyZeroPointRvCorrection;

        public Options() {
            format = fileFormats.stream()
                    .filter(FileFormat::isDefault)
                    .findFirst()
                    .orElse(fileFormats.get(0));
            applyZeroPointRvCorrection = true;
        }
    }
}
