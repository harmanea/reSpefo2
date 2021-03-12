package cz.cuni.mff.respefo.function.scan;

import cz.cuni.mff.respefo.component.ComponentManager;
import cz.cuni.mff.respefo.function.Fun;
import cz.cuni.mff.respefo.function.SingleFileFunction;
import cz.cuni.mff.respefo.function.filter.FitsFileFilter;
import cz.cuni.mff.respefo.util.Message;
import nom.tam.fits.Fits;
import nom.tam.fits.FitsException;
import nom.tam.fits.Header;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Table;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;

import static cz.cuni.mff.respefo.util.builders.widgets.TableBuilder.newTable;
import static java.util.Optional.ofNullable;

@Fun(name = "Inspect FITS Header", fileFilter = FitsFileFilter.class, group = "FITS")
public class FitsHeaderFunction implements SingleFileFunction {
    @Override
    public void execute(File file) {
        try (Fits f = new Fits(file)) {
            Header header = f.getHDU(0).getHeader();
            newTable(SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL)
                    .gridLayoutData(GridData.FILL_BOTH)
                    .linesVisible(true)
                    .headerVisible(true)
                    .listener(SWT.KeyDown, event -> {
                        if (event.stateMask == SWT.CTRL && (event.keyCode == 'c' || event.keyCode == 'C') && ((Table) event.widget).getSelectionCount() > 0) {
                            Clipboard clipboard = new Clipboard(ComponentManager.getDisplay());
                            clipboard.setContents(new Object[]{getTextFromSelectedRow(((Table) event.widget))}, new Transfer[]{TextTransfer.getInstance()});
                            clipboard.dispose();
                        }
                    })
                    .columns("Key", "Value", "Comment")
                    .items(header::iterator, card -> new String[]{
                            ofNullable(card.getKey()).orElse(""),
                            ofNullable(card.getValue()).orElse(""),
                            ofNullable(card.getComment()).orElse("")
                    })
                    .packColumns()
                    .build(ComponentManager.clearAndGetScene());

            ComponentManager.getScene().layout();

        } catch (FitsException | IOException exception) {
            Message.error("An error occurred while reading FITS file", exception);
        }
    }

    private static String getTextFromSelectedRow(Table table) {
        return Arrays.stream(table.getSelection())
                .map(item -> String.join("\t", item.getText(0), item.getText(1), item.getText(2)))
                .collect(Collectors.joining("\n"));
    }
}
