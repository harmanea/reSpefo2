package cz.cuni.mff.respefo.format;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import cz.cuni.mff.respefo.SpefoException;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import static java.util.stream.Collectors.toList;

public class Spectrum {
    private static final ObjectMapper mapper = new ObjectMapper();

    private final SpectrumFile spectrumFile;
    private final File file;

    public Spectrum(File file) throws SpefoException {
        spectrumFile = readFile(file);
        this.file = file;
    }

    public void save() throws SpefoException {
        saveAs(file);
    }

    public void saveAs(File file) throws SpefoException {
        saveFile(file, spectrumFile);
    }

    public File getFile() {
        return file;
    }

    public Data getRawData() {
        return spectrumFile.getData();
    }

    public Data getProcessedData() {
        Data data = spectrumFile.getData();

        Collection<DataAlteringFunctionAsset> dataAlteringFunctionAssets = spectrumFile.getFunctionAssets().values()
                .stream()
                .filter(asset -> asset instanceof DataAlteringFunctionAsset)
                .map(asset -> (DataAlteringFunctionAsset) asset)
                .collect(toList());

        for (DataAlteringFunctionAsset asset : dataAlteringFunctionAssets) {
            data = asset.process(data);
        }

        return data;
    }

    private static SpectrumFile readFile(File file) throws SpefoException {
        try {
            return mapper.readValue(file, SpectrumFile.class);

        } catch (JsonParseException | JsonMappingException exception) {
            throw new SpefoException("An error occurred while processing JSON.", exception);
        } catch (IOException exception) {
            throw new SpefoException("An error occurred while reading file.", exception);
        }
    }

    private static void saveFile(File destinationFile, SpectrumFile spectrumFile) throws SpefoException {
        try {
            ObjectWriter writer = mapper.writer();
            writer.writeValue(destinationFile, spectrumFile);

        } catch (JsonGenerationException | JsonMappingException exception) {
            throw new SpefoException("An error occurred while processing JSON.", exception);
        } catch (IOException exception) {
            throw new SpefoException("An error occurred while writing to file.", exception);
        }
    }
}
