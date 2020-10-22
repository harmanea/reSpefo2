package cz.cuni.mff.respefo.component;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.*;

import java.util.function.Consumer;

import static cz.cuni.mff.respefo.util.builders.GridDataBuilder.gridData;
import static cz.cuni.mff.respefo.util.builders.GridLayoutBuilder.gridLayout;

public class VerticalToggle extends Composite {

    private final boolean up;

    private final Canvas canvas;
    private final Label label;

    private String text;
    private Consumer<Boolean> toggleAction = t -> {};

    private boolean toggled;

    /**
     * @param parent a widget which will be the parent of the new instance (cannot be null)
     * @param style use either SWT.UP or SWT.DOWN for text orientation
     */
    public VerticalToggle(Composite parent, int style) {
        super(parent, style);

        up = (style & SWT.DOWN) == 0;

        setDefaultBackground();
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

        Listener mouseEnterListener = event -> {
            if (!toggled) {
                setHighlightedBackground();
            }
        };
        Listener mouseExitListener = event -> {
            if (!toggled) {
                setDefaultBackground();
            }
        };
        Listener mouseUpListener = event -> toggle();
        for (Widget widget : new Widget[] {this, label, canvas}) {
            widget.addListener(SWT.MouseEnter, mouseEnterListener);
            widget.addListener(SWT.MouseExit, mouseExitListener);
            widget.addListener(SWT.MouseUp, mouseUpListener);
        }
    }

    public void setText(String text) {
        this.text = text;

        GC gc = new GC(label);
        Point point = gc.stringExtent(text);
        gc.dispose();

        canvas.setLayoutData(gridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING)
                .widthHint(point.y).heightHint(point.x).build());
    }

    public void setImage(Image image) {
        label.setImage(image);
    }

    public void setToggleAction(Consumer<Boolean> toggleAction) {
        this.toggleAction = toggleAction;
    }

    public void setToggled(boolean toggled) {
        if (toggled != this.toggled) {
            if (toggled) {
                setToggledBackground();
            } else {
                setDefaultBackground();
            }

            this.toggled = toggled;
        }
    }

    public void toggle() {
        setToggled(!toggled);
        toggleAction.accept(toggled);
    }

    private void setDefaultBackground() {
        setBackground(getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
    }

    private void setHighlightedBackground() {
        setBackground(getDisplay().getSystemColor(SWT.COLOR_WIDGET_HIGHLIGHT_SHADOW));
    }

    private void setToggledBackground() {
        setBackground(getDisplay().getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW));
    }
}
