package cz.cuni.mff.respefo.component;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Widget;

import static cz.cuni.mff.respefo.util.builders.GridDataBuilder.gridData;
import static cz.cuni.mff.respefo.util.builders.GridLayoutBuilder.gridLayout;

public class VerticalToggle extends Toggle {

    private final boolean up;

    private final Canvas canvas;
    private final Label label;

    private String text;

    /**
     * @param parent a widget which will be the parent of the new instance (cannot be null)
     * @param style use either SWT.UP or SWT.DOWN for text orientation
     */
    public VerticalToggle(Composite parent, int style) {
        super(parent, style & ~SWT.DOWN & ~SWT.UP);

        up = (style & SWT.DOWN) == 0;

        setBackgroundMode(SWT.INHERIT_FORCE);
        setLayout(gridLayout().marginTop(up ? 10 : 5).marginBottom(up ? 5 : 10).build());

        if (up) {
            canvas = new Canvas(this, SWT.NONE);
            label = new Label(this, SWT.NONE);
        } else {
            label = new Label(this, SWT.NONE);
            canvas = new Canvas(this, SWT.NONE);
        }

        canvas.setBackground(getDisplay().getSystemColor(SWT.COLOR_TRANSPARENT));
        canvas.addPaintListener(event -> {
            Transform transform = new Transform(event.display);
            transform.translate(up ? 0 : event.width, up ? event.height : 0);
            transform.rotate(up ? 270 : 90);
            event.gc.setTransform(transform);

            event.gc.setFont(label.getFont());
            event.gc.drawString(text, 0, 0, true);
            transform.dispose();
        });
        label.setLayoutData(new GridData());

        for (Widget widget : new Widget[] {this, label, canvas}) {
            addListeners(widget);
        }
    }

    @Override
    public void setText(String text) {
        this.text = text;

        GC gc = new GC(label);
        Point point = gc.stringExtent(text);
        gc.dispose();

        canvas.setLayoutData(gridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING)
                .widthHint(point.y).heightHint(point.x).build());
    }

    @Override
    public void setImage(Image image) {
        label.setImage(image);
    }
}
