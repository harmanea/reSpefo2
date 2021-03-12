package cz.cuni.mff.respefo.component;

import cz.cuni.mff.respefo.SpefoException;
import cz.cuni.mff.respefo.format.Spectrum;
import cz.cuni.mff.respefo.function.FunctionInfo;
import cz.cuni.mff.respefo.function.FunctionManager;
import cz.cuni.mff.respefo.function.MultiFileFunction;
import cz.cuni.mff.respefo.function.SingleFileFunction;
import cz.cuni.mff.respefo.function.asset.ew.MeasureEWResults;
import cz.cuni.mff.respefo.function.asset.rectify.RectifyAsset;
import cz.cuni.mff.respefo.function.asset.rv.MeasureRVResults;
import cz.cuni.mff.respefo.function.filter.SpefoFormatFileFilter;
import cz.cuni.mff.respefo.logging.Log;
import cz.cuni.mff.respefo.resources.ImageManager;
import cz.cuni.mff.respefo.resources.ImageResource;
import cz.cuni.mff.respefo.util.Progress;
import cz.cuni.mff.respefo.util.utils.FileUtils;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.widgets.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static cz.cuni.mff.respefo.util.builders.widgets.TableBuilder.newTable;
import static org.eclipse.swt.SWT.*;

// Proof of concept, it's code should eventually be mostly shared with FileExplorer
public class SpectrumExplorer {

    private static SpectrumExplorer defaultInstance;

    private final Table table;

    public static SpectrumExplorer getDefault() {
        return defaultInstance;
    }

    public static void setDefaultInstance(SpectrumExplorer defaultInstance) {
        SpectrumExplorer.defaultInstance = defaultInstance;
    }

    public SpectrumExplorer(Composite parent) {
        this.table = newTable(MULTI | V_SCROLL | VIRTUAL).columns(4).build(parent);

        addMenu();
    }

    public void setRootDirectory(File file) {
        for (TableItem item : table.getItems()) {
            item.dispose();
        }

        Progress.withProgressTracking(p -> {
            File[] lst = file.listFiles(new SpefoFormatFileFilter());
            if (lst == null) {
                throw new IllegalStateException("Couldn't list files for file " + file);
            }

            p.refresh("Loading spectrum files", lst.length);

            List<Spectrum> spectra = new ArrayList<>(lst.length);
            for (File spectrumFile : lst) {
                try {
                    Spectrum spectrum = Spectrum.open(spectrumFile);
                    spectra.add(spectrum);

                } catch (SpefoException exception) {
                    Log.error("Couldn't load file [" + spectrumFile.toString() + "]", exception);
                } finally {
                    p.step();
                }

            }

            spectra.sort(Spectrum.hjdComparator());
            return spectra;

        }, spectra -> {
            for (Spectrum spectrum : spectra) {
                TableItem item = new TableItem(table, NONE);
                item.setData(spectrum.getFile());
                item.setText(0, FileUtils.stripParent(spectrum.getFile().getName()));
                item.setImage(0, ImageManager.getImage(ImageResource.SPECTRUM_FILE));

                if (spectrum.getFunctionAsset("rectify", RectifyAsset.class).isPresent()) {
                    item.setImage(1, ImageManager.getImage(ImageResource.RECTIFY));
                }

                if (spectrum.getFunctionAsset("rv", MeasureRVResults.class).isPresent()) {
                    item.setImage(2, ImageManager.getImage(ImageResource.RV));
                }

                if (spectrum.getFunctionAsset("ew", MeasureEWResults.class).isPresent()) {
                    item.setImage(3, ImageManager.getImage(ImageResource.EW));
                }
            }

            for (TableColumn column : table.getColumns()) {
                column.pack();
            }
        });
    }

    public void setLayoutData(Object layoutData) {
        table.setLayoutData(layoutData);
    }

    public void refresh() {
        setRootDirectory(Project.getRootDirectory()); // TODO: optimize this
    }

    private void addMenu() {
        final Menu menu = new Menu(ComponentManager.getShell(), POP_UP | NO_RADIO_GROUP);
        table.setMenu(menu);

        menu.addMenuListener(new MenuAdapter() {

            @Override
            public void menuShown(MenuEvent e) {
                for (MenuItem item : menu.getItems()) {
                    item.dispose();
                }

                if (table.getSelectionCount() == 1) {
                    File selectedFile = (File) table.getSelection()[0].getData();

                    for (FunctionInfo<SingleFileFunction> fun : FunctionManager.getSingleFileFunctions()) {
                        if (fun.getFileFilter() instanceof SpefoFormatFileFilter) {
                            MenuItem item = new MenuItem(menu, PUSH);
                            item.setText(fun.getName());
                            item.addListener(Selection, event -> fun.getInstance().execute(selectedFile));
                        }
                    }

                } else if (table.getSelectionCount() > 1) {
                    List<File> selectedFiles = Arrays.stream(table.getSelection())
                            .map(item -> (File) item.getData())
                            .collect(Collectors.toList());

                    for (FunctionInfo<MultiFileFunction> fun : FunctionManager.getMultiFileFunctions()) {
                        if (fun.getFileFilter() instanceof SpefoFormatFileFilter) {
                            MenuItem item = new MenuItem(menu, PUSH);
                            item.setText(fun.getName());
                            item.addListener(Selection, event -> fun.getInstance().execute(selectedFiles));
                        }
                    }
                }
            }
        });
    }
}
