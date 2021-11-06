package cz.cuni.mff.respefo.function.filter;

import com.fasterxml.jackson.databind.JsonNode;
import cz.cuni.mff.respefo.spectrum.Spectrum;
import cz.cuni.mff.respefo.spectrum.format.SimpleSpectrum;
import cz.cuni.mff.respefo.util.utils.FileUtils;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

public class SimpleSpectrumFileFilter implements FileFilter {
    @Override
    public boolean accept(File pathname) {
        if (pathname.isDirectory() || !FileUtils.getFileExtension(pathname).equals("spf")) {
            return false;
        }

        try {
            JsonNode root = Spectrum.MAPPER.readTree(pathname);
            int format = root.get("format").asInt();
            return format == SimpleSpectrum.FORMAT;

        } catch (IOException e) {
            return false;
        }
    }
}
