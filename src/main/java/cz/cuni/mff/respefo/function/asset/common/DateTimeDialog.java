package cz.cuni.mff.respefo.function.asset.common;

import cz.cuni.mff.respefo.component.SpefoDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DateTime;

import java.time.LocalDateTime;

import static cz.cuni.mff.respefo.util.builders.GridLayoutBuilder.gridLayout;
import static cz.cuni.mff.respefo.util.builders.widgets.CompositeBuilder.newComposite;

public class DateTimeDialog extends SpefoDialog {

    private LocalDateTime dateTime;

    private DateTime time;
    private DateTime calendar;

    public DateTimeDialog() {
        this(LocalDateTime.now());
    }

    public DateTimeDialog(LocalDateTime dateTime) {
        super("Select time and date");
        this.dateTime = dateTime;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    @Override
    protected void createDialogArea(Composite parent) {
        final Composite composite = newComposite()
                .layout(gridLayout().margins(15).horizontalSpacing(10))
                .gridLayoutData(GridData.FILL_BOTH)
                .build(parent);

        time = new DateTime(composite, SWT.TIME | SWT.MEDIUM);
        time.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING));
        time.setTime(dateTime.getHour(), dateTime.getMinute(), dateTime.getSecond());

        calendar = new DateTime(composite, SWT.CALENDAR);
        calendar.setLayoutData(new GridData(GridData.FILL_BOTH));
        calendar.setDate(dateTime.getYear(), dateTime.getMonthValue() - 1, dateTime.getDayOfMonth());
    }

    @Override
    protected void buttonPressed(int returnCode) {
        if (returnCode == SWT.OK) {
            dateTime = dateTime
                    .withYear(calendar.getYear())
                    .withMonth(calendar.getMonth() + 1)
                    .withDayOfMonth(calendar.getDay())
                    .withHour(time.getHours())
                    .withMinute(time.getMinutes())
                    .withSecond(time.getSeconds());
        }

        super.buttonPressed(returnCode);
    }
}
