package cz.cuni.mff.respefo.format;

import cz.cuni.mff.respefo.format.formats.ExportFileFormat;
import cz.cuni.mff.respefo.format.formats.FileFormat;
import cz.cuni.mff.respefo.format.formats.ImportFileFormat;
import cz.cuni.mff.respefo.logging.Log;
import cz.cuni.mff.respefo.util.UtilityClass;
import cz.cuni.mff.respefo.util.utils.FileUtils;
import org.reflections.Reflections;

import java.util.*;

public class FormatManager extends UtilityClass {
    private static final String PACKAGE_TO_SCAN = "cz.cuni.mff.respefo.format.formats";
    private static Map<String, List<ImportFileFormat>> importFileFormats;
    private static Map<String, List<ExportFileFormat>> exportFileFormats;

    public static void scan() {
        Reflections reflections = new Reflections(PACKAGE_TO_SCAN);

        importFileFormats = scanForFileFormats(reflections, ImportFileFormat.class);
        exportFileFormats = scanForFileFormats(reflections, ExportFileFormat.class);
    }

    public static <T extends FileFormat> Map<String, List<T>> scanForFileFormats(Reflections reflections, Class<T> cls) {
        Map<String, List<T>> fileFormatsMap = new HashMap<>();
        Set<Class<? extends T>> fileFormatClasses = reflections.getSubTypesOf(cls);

        for (Class<? extends T> fileFormatClass : fileFormatClasses) {
            try {
                T instance = fileFormatClass.getDeclaredConstructor().newInstance();
                for (String fileExtension : instance.fileExtensions()) {
                    fileFormatsMap.computeIfAbsent(fileExtension, k -> new ArrayList<>()).add(instance);
                }

            } catch (Exception exception) {
                Log.error("Error while loading file format class [%s].", exception, fileFormatClass);
            }
        }

        if (fileFormatsMap.isEmpty()) {
            Log.warning("No formats of type " + cls.getSimpleName() + " were loaded");
        }

        return fileFormatsMap;
    }

    public static Set<String> getImportableFileExtensions() {
        return importFileFormats.keySet();
    }

    public static List<ImportFileFormat> getImportFileFormats(String fileName) throws UnknownFileFormatException {
        String fileExtension = FileUtils.getFileExtension(fileName);

        if (!importFileFormats.containsKey(fileExtension)) {
            throw new UnknownFileFormatException("Unknown file extension [" + fileExtension + "].");
        }

        return importFileFormats.get(fileExtension);
    }

    public static List<ExportFileFormat> getExportFileFormats(String fileName) throws UnknownFileFormatException {
        String fileExtension = FileUtils.getFileExtension(fileName);

        if (!exportFileFormats.containsKey(fileExtension)) {
            throw new UnknownFileFormatException("Unknown file extension [" + fileExtension + "].");
        }

        return exportFileFormats.get(fileExtension);
    }

    protected FormatManager() throws IllegalAccessException {
        super();
    }
}
