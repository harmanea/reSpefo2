package cz.cuni.mff.respefo.function.lst;

import cz.cuni.mff.respefo.component.ComponentManager;
import cz.cuni.mff.respefo.exception.SpefoException;
import cz.cuni.mff.respefo.function.Fun;
import cz.cuni.mff.respefo.function.SingleFileFunction;
import cz.cuni.mff.respefo.function.filter.LstFileFilter;
import cz.cuni.mff.respefo.util.Message;
import cz.cuni.mff.respefo.util.utils.FileUtils;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

import java.io.File;

import static cz.cuni.mff.respefo.function.lst.LstFile.DATE_TIME_FORMATTER;
import static cz.cuni.mff.respefo.util.layout.GridLayoutBuilder.gridLayout;
import static cz.cuni.mff.respefo.util.utils.FormattingUtils.formatDouble;
import static cz.cuni.mff.respefo.util.utils.FormattingUtils.formatInteger;
import static cz.cuni.mff.respefo.util.widget.CompositeBuilder.newComposite;
import static cz.cuni.mff.respefo.util.widget.LabelBuilder.newLabel;
import static cz.cuni.mff.respefo.util.widget.TableBuilder.newTable;
import static cz.cuni.mff.respefo.util.widget.TextBuilder.newText;
import static java.util.Optional.ofNullable;
import static org.eclipse.swt.SWT.*;

@Fun(name = "Open .lst File", fileFilter = LstFileFilter.class)
public class OpenLstFileFunction implements SingleFileFunction {
    @Override
    public void execute(File file) {
        try {
            LstFile lstFile = LstFile.open(file);

            final Composite topComposite = newComposite()
                    .gridLayoutData(GridData.FILL_BOTH)
                    .layout(gridLayout().margins(0))
                    .background(ComponentManager.getDisplay().getSystemColor(COLOR_LIST_BACKGROUND))
                    .build(ComponentManager.clearAndGetScene());

            newLabel(CENTER)
                    .gridLayoutData(GridData.FILL_HORIZONTAL)
                    .text(FileUtils.getRelativePath(file).toString())
                    .bold()
                    .build(topComposite);

            final Composite textComposite = newComposite()
                    .gridLayoutData(GridData.FILL_HORIZONTAL)
                    .layout(gridLayout().margins(0).marginLeft(5))
                    .build(topComposite);

            newText(MULTI | READ_ONLY)
                    .gridLayoutData(GridData.FILL_HORIZONTAL)
                    .text(lstFile.getHeader().trim())
                    .editable(false)
                    .requestLayout()
                    .build(textComposite);

            newTable(MULTI | V_SCROLL | H_SCROLL)
                    .gridLayoutData(GridData.FILL_BOTH)
                    .linesVisible(true)
                    .headerVisible(true)
                    .columns("N.", "Date & UT start", "exp[s]", "Filename", "J.D.hel.", "RVcorr")
                    .items(lstFile, r -> new String[]{
                            formatInteger(r.getIndex(), 5),
                            r.getDateTimeStart().format(DATE_TIME_FORMATTER),
                            formatDouble(r.getExpTime(), 5, 3, false),
                            ofNullable(r.getFileName()).orElse(""),
                            formatDouble(r.getHjd().getJD(), 5, 4),
                            formatDouble(r.getRvCorr(), 3, 2)
                    })
                    .packColumns()
                    .build(topComposite);

            ComponentManager.getScene().layout();

        } catch (SpefoException exception) {
            Message.error("An error occurred while reading the file", exception);
        }
    }
}
