package cz.cuni.mff.respefo.util.builders;

import org.eclipse.swt.layout.GridData;

public class GridDataBuilder {
    private final GridData data;

    private GridDataBuilder() {
        data = new GridData();
    }

    private GridDataBuilder(int style) {
        data = new GridData(style);
    }

    public static GridDataBuilder gridData() {
        return new GridDataBuilder();
    }

    public static GridDataBuilder gridData(int style) {
        return new GridDataBuilder(style);
    }

    public GridDataBuilder verticalAlignment(int value) {
        data.verticalAlignment = value;

        return this;
    }

    public GridDataBuilder horizontalAlignment(int value) {
        data.horizontalAlignment = value;

        return this;
    }

    public GridDataBuilder widthHint(int value) {
        data.widthHint = value;

        return this;
    }

    public GridDataBuilder heightHint(int value) {
        data.heightHint = value;

        return this;
    }

    public GridDataBuilder horizontalSpan(int value) {
        data.horizontalSpan = value;

        return this;
    }

    public GridDataBuilder verticalSpan(int value) {
        data.verticalSpan = value;

        return this;
    }

    public GridData build() {
        return data;
    }
}
