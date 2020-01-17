package cz.cuni.mff.respefo.format;

import cz.cuni.mff.respefo.SpefoException;
import cz.cuni.mff.respefo.logging.Log;
import cz.cuni.mff.respefo.util.UtilityClass;
import cz.cuni.mff.respefo.util.utils.FileUtils;
import org.reflections.Reflections;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class FormatManager extends UtilityClass {
    private static final String PACKAGE_TO_SCAN = "cz.cuni.mff.respefo.format.scan";

    private static Map<String, FileFormat> fileFormats;

    public static void scan() {
        Reflections reflections = new Reflections(PACKAGE_TO_SCAN);
        Set<Class<? extends FileFormat>> formatClasses = reflections.getSubTypesOf(FileFormat.class);

        fileFormats = new HashMap<>();

        for (Class<? extends FileFormat> formatClass : formatClasses) {
            processFormatClass(formatClass);
        }

        if (fileFormats.isEmpty()) {
            Log.warning("No file formats were loaded.");
        }
    }

    private static void processFormatClass(Class<? extends FileFormat> formatClass) {
        try {
            FileFormat instance = formatClass.getDeclaredConstructor().newInstance();

            for (String fileExtension : instance.fileExtensions()) {
                fileFormats.put(fileExtension, instance);
            }

        } catch (Exception exception) {
            Log.error("Error while loading file format [%s].", exception, formatClass);
        }
    }

    public static Set<String> getKnownFileExtensions() {
        return fileFormats.keySet();
    }

    public static SpectrumFile importFrom(String fileName) throws SpefoException {
        String fileExtension = extractFileExtensionAndThrowIfKeyNotPresent(fileName);

        return fileFormats.get(fileExtension).importFrom(fileName);
    }

    public static void exportTo(SpectrumFile spectrumFile, String fileName) throws SpefoException {
        String fileExtension = extractFileExtensionAndThrowIfKeyNotPresent(fileName);

        fileFormats.get(fileExtension).exportTo(spectrumFile, fileName);
    }

    private static String extractFileExtensionAndThrowIfKeyNotPresent(String fileName) throws UnknownFileFormatException {
        String fileExtension = FileUtils.getFileExtension(fileName);

        if (!fileFormats.containsKey(fileExtension)) {
            throw new UnknownFileFormatException("Unknown file extension [" + fileExtension + "].");
        }

        return fileExtension;
    }

    protected FormatManager() throws IllegalAccessException {
        super();
    }
}
