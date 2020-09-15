package cz.cuni.mff.respefo.function.scan;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import cz.cuni.mff.respefo.function.Fun;
import cz.cuni.mff.respefo.function.SingleFileFunction;
import cz.cuni.mff.respefo.function.filter.SpefoFormatFileFilter;
import cz.cuni.mff.respefo.logging.Log;

import java.io.File;
import java.io.IOException;

@Fun(name = "__ Debug __", fileFilter = SpefoFormatFileFilter.class)
public class DebugFunction implements SingleFileFunction {
    @Override
    public void execute(File file) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode root = mapper.readTree(file);
            ((ObjectNode) root).put("data", "...");

            String prettyJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(root);

            Log.info("File header:\n" + prettyJson);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
