package cz.cuni.mff.respefo.function.debug;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import cz.cuni.mff.respefo.component.ComponentManager;
import cz.cuni.mff.respefo.function.Fun;
import cz.cuni.mff.respefo.function.SingleFileFunction;
import cz.cuni.mff.respefo.function.filter.SpefoFormatFileFilter;
import cz.cuni.mff.respefo.util.Message;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

@Fun(name = "Inspect JSON", fileFilter = SpefoFormatFileFilter.class, group = "Debug")
public class InspectJSONFunction implements SingleFileFunction {

    @Override
    public void execute(File file) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode root = mapper.readTree(file);
            ((ObjectNode) root).replace("series", null); // don't show the raw data

            final Tree tree = new Tree(ComponentManager.clearAndGetScene(), SWT.V_SCROLL | SWT.H_SCROLL);
            tree.setLayoutData(new GridData(GridData.FILL_BOTH));
            tree.addListener(SWT.Expand, event -> expandItem((TreeItem) event.item));
            tree.addListener(SWT.Collapse, event -> collapseItem((TreeItem) event.item));

            TreeItem rootNode = new TreeItem(tree, SWT.NONE);
            rootNode.setText("root");
            setupNode(rootNode, root);

            expandItem(rootNode);
            rootNode.setExpanded(true);

            ComponentManager.getScene().layout();

        } catch (IOException exception) {
            Message.error("An error occurred while opening file", exception);
        }
    }

    private static void expandItem(TreeItem item) {
        TreeItem parentItem = item.getParentItem();
        TreeItem closeItem;
        if (parentItem != null) {
            closeItem = new TreeItem(parentItem, SWT.NONE, parentItem.indexOf(item) + 1);
        } else {
            closeItem = new TreeItem(item.getParent(), SWT.NONE, item.getParent().indexOf(item) + 1);
        }

        String text = item.getText();
        item.setText(text.substring(0, text.length() - 1));
        closeItem.setText(text.substring(text.length() - 1));
    }

    private static void collapseItem(TreeItem item) {
        for (TreeItem childItem : item.getItems()) {
            if (!childItem.isDisposed() && childItem.getExpanded()) {
                collapseItem(childItem);
            }
        }

        TreeItem parentItem = item.getParentItem();
        TreeItem closeItem;
        if (parentItem != null) {
            closeItem = parentItem.getItem(parentItem.indexOf(item) + 1);
        } else {
            closeItem = item.getParent().getItem(item.getParent().indexOf(item) + 1);
        }

        item.setText(item.getText() + closeItem.getText());
        closeItem.dispose();
    }

    private void setupNode(TreeItem item, JsonNode node) {
        if (node.isValueNode()) {
            item.setText(item.getText() + ": " + node.asText());

        } else if (node.isObject()) {
            item.setText(item.getText() + ": {}");
            for (Iterator<Map.Entry<String, JsonNode>> it = node.fields(); it.hasNext(); ) {
                Map.Entry<String, JsonNode> entry = it.next();

                TreeItem childItem = new TreeItem(item, SWT.NONE);
                childItem.setText(entry.getKey());
                setupNode(childItem, entry.getValue());
            }

        } else if (node.isArray()) {
            item.setText(item.getText() + ": []");
            for (int i = 0; i < node.size(); i++) {
                TreeItem childItem = new TreeItem(item, SWT.NONE);
                childItem.setText(String.valueOf(i));
                setupNode(childItem, node.get(i));
            }
        } // else is missing
    }
}
