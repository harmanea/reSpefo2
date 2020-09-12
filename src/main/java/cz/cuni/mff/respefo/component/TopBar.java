package cz.cuni.mff.respefo.component;

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

import static cz.cuni.mff.respefo.util.builders.CompositeBuilder.composite;
import static cz.cuni.mff.respefo.util.builders.GridLayoutBuilder.gridLayout;
import static cz.cuni.mff.respefo.util.builders.LabelBuilder.label;
import static cz.cuni.mff.respefo.util.builders.RowLayoutBuilder.rowLayout;
import static org.eclipse.swt.SWT.*;

public class TopBar {
    private final Composite toolbox;

    public TopBar(Composite parent, String text) {
        final Composite composite = composite(parent, BORDER)
                .layout(gridLayout(2, false).margins(0).spacings(0).build())
                .layoutData(new GridData(FILL, TOP, true, false))
                .build();

        final Composite labelComposite = composite(composite, NONE)
                .layout(rowLayout(HORIZONTAL).margins(3).build())
                .layoutData(new GridData(FILL, CENTER, true, true))
                .build();

        label(labelComposite)
                .text(text)
                .bold()
                .build();

        toolbox = composite(composite, RIGHT_TO_LEFT)
                .layout(rowLayout(HORIZONTAL).margins(0).build())
                .layoutData(new GridData(GridData.FILL_VERTICAL | GridData.HORIZONTAL_ALIGN_END))
                .build();
    }

    public Composite getToolbox() {
        return toolbox;
    }
}
