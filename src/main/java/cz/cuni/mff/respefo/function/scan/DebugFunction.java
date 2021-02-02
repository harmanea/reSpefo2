package cz.cuni.mff.respefo.function.scan;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import cz.cuni.mff.respefo.component.ComponentManager;
import cz.cuni.mff.respefo.function.SingleFileFunction;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Text;

import java.io.File;
import java.io.IOException;

import static cz.cuni.mff.respefo.util.builders.widgets.TextBuilder.newText;
import static org.eclipse.swt.SWT.*;

//@Fun(name = "__ Debug __", fileFilter = SpefoFormatFileFilter.class)
public class DebugFunction implements SingleFileFunction {

    @Override
    public void execute(File file) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode root = mapper.readTree(file);
            ((ObjectNode) root).replace("series", null); // don't show the raw data

            final Text text = newText(MULTI | READ_ONLY | WRAP | V_SCROLL)
                    .gridLayoutData(GridData.FILL_BOTH)
                    .text(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(root))
                    .build(ComponentManager.clearAndGetScene());
            text.requestLayout();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
