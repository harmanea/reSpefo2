package cz.cuni.mff.respefo.function.scan;

import cz.cuni.mff.respefo.component.ComponentManager;
import cz.cuni.mff.respefo.function.Fun;
import cz.cuni.mff.respefo.function.SingleFileFunction;
import cz.cuni.mff.respefo.function.filter.PlainTextFileFilter;
import cz.cuni.mff.respefo.util.Message;
import cz.cuni.mff.respefo.util.builders.widgets.CompositeBuilder;
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
import static cz.cuni.mff.respefo.util.builders.widgets.CompositeBuilder.newComposite;
import static cz.cuni.mff.respefo.util.builders.widgets.LabelBuilder.newLabel;
import static cz.cuni.mff.respefo.util.builders.widgets.TextBuilder.newText;
import static org.eclipse.swt.SWT.*;

@Fun(name = "Open Plain Text", fileFilter = PlainTextFileFilter.class)
public class OpenPlainTextFunction implements SingleFileFunction {
    @Override
    public void execute(File file) {
        try {
            String content = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);

            CompositeBuilder compositeBuilder = newComposite(BORDER).gridLayoutData(GridData.FILL_BOTH);

            final Composite topComposite = compositeBuilder.layout(new GridLayout()).build(ComponentManager.clearAndGetScene());

            newLabel(CENTER)
                    .gridLayoutData(GridData.FILL_HORIZONTAL)
                    .text(FileUtils.getRelativePath(file).toString())
                    .bold()
                    .build(topComposite);

            final Composite textComposite = compositeBuilder
                    .layout(gridLayout().margins(0).marginLeft(5))
                    .background(ComponentManager.getDisplay().getSystemColor(COLOR_WIDGET_BACKGROUND))
                    .build(topComposite);

            final Text text = newText(MULTI | READ_ONLY | WRAP | V_SCROLL)
                    .gridLayoutData(GridData.FILL_BOTH)
                    .text(content)
                    .build(textComposite);
            text.requestLayout();

        } catch (IOException exception) {
            Message.error("An error occurred while reading the file", exception);
        }
    }
}
