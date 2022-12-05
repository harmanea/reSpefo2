package cz.cuni.mff.respefo.function.debug;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import cz.cuni.mff.respefo.component.ComponentManager;
import cz.cuni.mff.respefo.function.SingleFileFunction;
import cz.cuni.mff.respefo.resources.ImageManager;
import cz.cuni.mff.respefo.resources.ImageResource;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

//@Fun(name = "Inspect JSON v2", fileFilter = SpefoFormatFileFilter.class, group = "Debug")
public class NewInspectJSONFunction implements SingleFileFunction {

    private static final int IMAGE_MARGIN = 2;

    @Override
    public void execute(File file) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode root = mapper.readTree(file);
            ((ObjectNode) root).replace("series", null); // don't show the raw data

            final Tree tree = new Tree(ComponentManager.clearAndGetScene(), SWT.V_SCROLL | SWT.H_SCROLL);
            tree.setLayoutData(new GridData(GridData.FILL_BOTH));

            TreeItem rootNode = new TreeItem(tree, SWT.NONE);
            setupNode(rootNode, root);

//            for (Iterator<Map.Entry<String, JsonNode>> it = root.fields(); it.hasNext(); ) {
//                Map.Entry<String, JsonNode> entry = it.next();
//
//                JsonNode node = entry.getValue();
//                TreeItem item = new TreeItem(tree, 0);
//
//                if (node.isValueNode()) {
//                    item.setText(entry.getKey() + ": " + node.asText());
//                } else if (node.isArray()) {
//                    item.setText(entry.getKey());
//                    item.setData(ImageManager.getImage(ImageResource.ARRAY));
//                    setupArrayNode(item, node);
//                } else if (node.isObject()) {
//                    item.setText(entry.getKey());
//                    item.setData(ImageManager.getImage(ImageResource.OBJECT));
//                }
//            }

            tree.addListener(SWT.MeasureItem, event -> {
                TreeItem item = (TreeItem) event.item;
                Image trailingImage = (Image) item.getData();
                if (trailingImage != null) {
                    event.width += trailingImage.getBounds().width + IMAGE_MARGIN;
                }
            });

            tree.addListener(SWT.PaintItem, event -> {
                TreeItem item = (TreeItem) event.item;
                Image trailingImage = (Image) item.getData();
                if (trailingImage != null) {
                    int x = event.x + event.width + IMAGE_MARGIN;
                    int itemHeight = tree.getItemHeight();
                    int imageHeight = trailingImage.getBounds().height;
                    int y = event.y + (itemHeight - imageHeight) / 2;
                    event.gc.drawImage(trailingImage, x, y);
                }
            });

            ComponentManager.getScene().layout();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setupNode(TreeItem item, JsonNode node) {
        if (node.isValueNode()) {

        } else if (node.isObject()) {

        } else if (node.isArray()) {

        } // else is missing
    }

    private void setupArrayNode(TreeItem parent, JsonNode arrayNode) {
        for (int i = 0; i < arrayNode.size(); i++) {
            JsonNode childNode = arrayNode.get(i);
            TreeItem item = new TreeItem(parent, 0);
            if (childNode.isValueNode()) {
                item.setText(childNode.asText());
            } else if (childNode.isArray()) {
                // TODO
            } else if (childNode.isObject()) {
                // TODO
            }
        }
    }

    private void setupChildNode(TreeItem parent, JsonNode node) {
        if (node.isValueNode()) {
            parent.setText(parent.getText() + ": " + node.asText());
            // TODO: what if parent is an array?
        } else if (node.isArray()) {
            parent.setImage(ImageManager.getImage(ImageResource.ARRAY));
            for (int i = 0; i < node.size(); i++) {
                JsonNode childNode = node.get(i);
                if (childNode.isObject()) {
                    TreeItem item = new TreeItem(parent, 0);
                    item.setText(String.valueOf(i));
                    setupChildNode(item, childNode);
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
