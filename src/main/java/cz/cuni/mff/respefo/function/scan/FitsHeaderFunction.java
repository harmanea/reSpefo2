package cz.cuni.mff.respefo.function.scan;

import cz.cuni.mff.respefo.component.ComponentManager;
import cz.cuni.mff.respefo.function.Fun;
import cz.cuni.mff.respefo.function.SingleFileFunction;
import cz.cuni.mff.respefo.function.filter.FitsFileFilter;
import cz.cuni.mff.respefo.util.Message;
import nom.tam.fits.Fits;
import nom.tam.fits.FitsException;
import nom.tam.fits.Header;
import nom.tam.fits.HeaderCard;
import nom.tam.util.Cursor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;

@Fun(name = "Inspect FITS Header", fileFilter = FitsFileFilter.class)
public class FitsHeaderFunction implements SingleFileFunction {
    @Override
    public void execute(File file) {
        try (Fits f = new Fits(file)) {
            Header header = f.getHDU(0).getHeader();

            Table table = new Table(ComponentManager.clearAndGetScene(), SWT.MULTI | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
            table.setLayoutData(new GridData(GridData.FILL_BOTH));
            table.setLinesVisible(true);
            table.setHeaderVisible(true);

            table.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    if (e.stateMask == SWT.CTRL && (e.keyCode == 'c' || e.keyCode == 'C')) {
                        if (table.getSelectionCount() > 0) {
                            Clipboard clipboard = new Clipboard(ComponentManager.getDisplay());
                            clipboard.setContents(new Object[]{ getTextFromSelectedRow(table) }, new Transfer[]{TextTransfer.getInstance()});
                            clipboard.dispose();
                        }
                    }
                }
            });

            String[] titles = {"Key", "Value", "Comment"};
            for (String title : titles) {
                TableColumn tableColumn = new TableColumn(table, SWT.NONE);
                tableColumn.setText(title);
            }

            for (Cursor<String, HeaderCard> it = header.iterator(); it.hasNext(); ) {
                HeaderCard card = it.next();

                TableItem tableItem = new TableItem(table, SWT.NONE);
                tableItem.setText(0, ofNullable(card.getKey()).orElse(""));
                tableItem.setText(1, ofNullable(card.getValue()).orElse(""));
                tableItem.setText(2, ofNullable(card.getComment()).orElse(""));
            }

            for (int i = 0; i < titles.length; i++) {
                table.getColumn(i).pack();
            }

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
