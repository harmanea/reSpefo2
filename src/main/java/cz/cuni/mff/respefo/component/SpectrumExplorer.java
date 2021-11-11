package cz.cuni.mff.respefo.component;

import cz.cuni.mff.respefo.exception.SpefoException;
import cz.cuni.mff.respefo.function.FunctionInfo;
import cz.cuni.mff.respefo.function.FunctionManager;
import cz.cuni.mff.respefo.function.MultiFileFunction;
import cz.cuni.mff.respefo.function.SingleFileFunction;
import cz.cuni.mff.respefo.function.filter.SpefoFormatFileFilter;
import cz.cuni.mff.respefo.logging.Log;
import cz.cuni.mff.respefo.resources.ImageManager;
import cz.cuni.mff.respefo.resources.ImageResource;
import cz.cuni.mff.respefo.spectrum.Spectrum;
import cz.cuni.mff.respefo.spectrum.format.EchelleSpectrum;
import cz.cuni.mff.respefo.util.Progress;
import cz.cuni.mff.respefo.util.utils.FileUtils;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.widgets.*;

import java.io.File;
import java.util.List;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static cz.cuni.mff.respefo.util.widget.TableBuilder.newTable;
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

        addDoubleClickListener();

        new SpectrumExplorerMenu();
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

                if (spectrum.containsFunctionAsset("rectify")
                        || (spectrum.getFormat() == EchelleSpectrum.FORMAT && !((EchelleSpectrum) spectrum).getRectifyAssets().isEmpty())) {
                    item.setImage(1, ImageManager.getImage(ImageResource.RECTIFY));
                }

                if (spectrum.containsFunctionAsset("rv")) {
                    item.setImage(2, ImageManager.getImage(ImageResource.RV));
                }

                if (spectrum.containsFunctionAsset("ew")) {
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

    private void addDoubleClickListener() {
        table.addListener(MouseDoubleClick, event -> {
            if (table.getSelectionCount() == 1) {
                TableItem item = table.getSelection()[0];

                File file = (File) item.getData();
                FunctionManager.getSingleFileFunctionByName("Open").execute(file);
            }
        });
    }

    private class SpectrumExplorerMenu {
        private final Menu menu;

        private final List<FunctionInfo<SingleFileFunction>> singleFileFunctions;
        private final List<String> singleFileGroups;
        private final List<FunctionInfo<MultiFileFunction>> multiFileFunctions;
        private final List<String> multiFileGroups;

        private int lastSelectionCount;

        public SpectrumExplorerMenu() {
            menu = new Menu(ComponentManager.getShell(), POP_UP | NO_RADIO_GROUP);
            table.setMenu(menu);

            singleFileFunctions = FunctionManager.getSingleFileFunctions().stream()
                    .filter(fun -> fun.getFileFilter() instanceof SpefoFormatFileFilter)
                    .collect(Collectors.toList());

            singleFileGroups = singleFileFunctions.stream()
                    .filter(fun -> fun.getGroup().isPresent())
                    .map(fun -> fun.getGroup().get())
                    .distinct()
                    .sorted()
                    .collect(Collectors.toList());

            multiFileFunctions = FunctionManager.getMultiFileFunctions().stream()
                    .filter(fun -> fun.getFileFilter() instanceof SpefoFormatFileFilter)
                    .collect(Collectors.toList());

            multiFileGroups = multiFileFunctions.stream()
                    .filter(fun -> fun.getGroup().isPresent())
                    .map(fun -> fun.getGroup().get())
                    .distinct()
                    .sorted()
                    .collect(Collectors.toList());

            lastSelectionCount = 0;

            menu.addMenuListener(new MenuAdapter() {
                @Override
                public void menuShown(MenuEvent e) {
                    int selectionCount = table.getSelectionCount();
                    if (selectionCount == 1 && lastSelectionCount != 1) {
                        createMenuItems(SpectrumExplorerMenu.this::getSingleSelection, singleFileGroups, singleFileFunctions, SingleFileFunction::execute);
                    } else if (selectionCount > 1 && lastSelectionCount <= 1) {
                        createMenuItems(SpectrumExplorerMenu.this::getMultiSelection, multiFileGroups, multiFileFunctions, MultiFileFunction::execute);
                    } else if (selectionCount == 0 && lastSelectionCount > 0) {
                        disposeItems();
                    }

                    lastSelectionCount = selectionCount;
                }
            });
        }

        private void disposeItems() {
            for (MenuItem item : menu.getItems()) {
                item.dispose();
            }
        }

        private File getSingleSelection() {
            return (File) table.getSelection()[0].getData();
        }

        private List<File> getMultiSelection() {
            return Arrays.stream(table.getSelection())
                    .map(item -> (File) item.getData())
                    .collect(Collectors.toList());
        }

        private <S, T> void createMenuItems(Supplier<S> selection, List<String> groups, List<FunctionInfo<T>> functions, BiConsumer<T, S> executor) {
            disposeItems();

            Map<String, Menu> menuGroups = new HashMap<>();

            for (String group : groups) {
                final MenuItem groupItem = new MenuItem(menu, CASCADE);
                groupItem.setText(group);

                final Menu subMenu = new Menu(ComponentManager.getShell(), DROP_DOWN | NO_RADIO_GROUP);
                groupItem.setMenu(subMenu);

                menuGroups.put(group, subMenu);
            }

            new MenuItem(menu, SEPARATOR);

            for (FunctionInfo<T> fun : functions) {
                MenuItem item = new MenuItem(fun.getGroup().isPresent() ? menuGroups.get(fun.getGroup().get()) : menu, PUSH);
                item.setText(fun.getName());
                item.addListener(Selection, event -> executor.accept(fun.getInstance(), selection.get()));
            }
        }
    }
}
