package cz.cuni.mff.respefo.function.scan;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import cz.cuni.mff.respefo.component.ComponentManager;
import cz.cuni.mff.respefo.function.Fun;
import cz.cuni.mff.respefo.function.SingleFileFunction;
import cz.cuni.mff.respefo.function.filter.SpefoFormatFileFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

@Fun(name = "__ Debug __", fileFilter = SpefoFormatFileFilter.class)
public class DebugFunction implements SingleFileFunction {
    @Override
    public void execute(File file) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode root = mapper.readTree(file);

            final Tree tree = new Tree(ComponentManager.clearAndGetScene(), SWT.V_SCROLL | SWT.H_SCROLL);
            tree.setLayoutData(new GridData(GridData.FILL_BOTH));

            for (Iterator<Map.Entry<String, JsonNode>> it = root.fields(); it.hasNext(); ) {
                Map.Entry<String, JsonNode> entry = it.next();
                TreeItem item = new TreeItem(tree, 0);
                item.setText(entry.getKey());
                setupChildNode(item, entry.getValue());
            }
            ComponentManager.getScene().layout();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setupChildNode(TreeItem parent, JsonNode node) {
        if (node.isValueNode()) {
            TreeItem item = new TreeItem(parent, 0);
            item.setText(node.asText());
        } else if (node.isArray()) {
            for (Iterator<JsonNode> it = node.elements(); it.hasNext(); ) {
                JsonNode childNode = it.next();
                if (childNode.isObject()) {
                    setupChildNode(new TreeItem(parent, 0), childNode);
                } else {
                    setupChildNode(parent, childNode);
                }
            }
        } else if (node.isObject()) {
            for (Iterator<Map.Entry<String, JsonNode>> it = node.fields(); it.hasNext(); ) {
                Map.Entry<String, JsonNode> entry = it.next();
                TreeItem item = new TreeItem(parent, 0);
                item.setText(entry.getKey());
                setupChildNode(item, entry.getValue());
            }
        }
    }
}
