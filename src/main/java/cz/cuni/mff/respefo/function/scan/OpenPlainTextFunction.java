package cz.cuni.mff.respefo.function.scan;

import cz.cuni.mff.respefo.component.ComponentManager;
import cz.cuni.mff.respefo.function.Fun;
import cz.cuni.mff.respefo.function.SingleFileFunction;
import cz.cuni.mff.respefo.function.filter.PlainTextFileFilter;
import cz.cuni.mff.respefo.util.Message;
import cz.cuni.mff.respefo.util.utils.FileUtils;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static cz.cuni.mff.respefo.util.builders.GridLayoutBuilder.gridLayout;
import static cz.cuni.mff.respefo.util.builders.LabelBuilder.label;
import static org.eclipse.swt.SWT.*;

@Fun(name = "Open plain text", fileFilter = PlainTextFileFilter.class)
public class OpenPlainTextFunction implements SingleFileFunction {
    @Override
    public void execute(File file) {
        try {
            String content = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);

            Composite topComposite = new Composite(ComponentManager.clearAndGetScene(), BORDER);
            topComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
            topComposite.setLayout(new GridLayout());

            label(topComposite, CENTER)
                    .text(FileUtils.getRelativePath(file).toString())
                    .bold()
                    .layoutData(new GridData(GridData.FILL_HORIZONTAL))
                    .build();

            Composite textComposite = new Composite(topComposite, BORDER);
            textComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
            textComposite.setLayout(gridLayout().margins(0).marginLeft(5).build());

            Text text = new Text(textComposite, MULTI | READ_ONLY | WRAP | V_SCROLL);
            text.setLayoutData(new GridData(GridData.FILL_BOTH));

            textComposite.setBackground(text.getBackground());

            text.setText(content);
            text.requestLayout();

        } catch (IOException exception) {
            Message.error("An error occurred while reading the file", exception);
        }
    }
}
