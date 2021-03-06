package cz.cuni.mff.respefo.function.open;

import cz.cuni.mff.respefo.component.ComponentManager;
import cz.cuni.mff.respefo.function.Fun;
import cz.cuni.mff.respefo.function.SingleFileFunction;
import cz.cuni.mff.respefo.function.filter.PlainTextFileFilter;
import cz.cuni.mff.respefo.util.Message;
import cz.cuni.mff.respefo.util.utils.FileUtils;
import cz.cuni.mff.respefo.util.widget.CompositeBuilder;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static cz.cuni.mff.respefo.util.layout.GridLayoutBuilder.gridLayout;
import static cz.cuni.mff.respefo.util.widget.CompositeBuilder.newComposite;
import static cz.cuni.mff.respefo.util.widget.LabelBuilder.newLabel;
import static cz.cuni.mff.respefo.util.widget.TextBuilder.newText;
import static org.eclipse.swt.SWT.*;

@Fun(name = "Open Plain Text", fileFilter = PlainTextFileFilter.class)
public class OpenPlainTextFunction implements SingleFileFunction {
    @Override
    public void execute(File file) {
        try {
            String content = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);

            CompositeBuilder compositeBuilder = newComposite().gridLayoutData(GridData.FILL_BOTH);

            final Composite topComposite = compositeBuilder
                    .layout(gridLayout().margins(0))
                    .background(ComponentManager.getDisplay().getSystemColor(COLOR_LIST_BACKGROUND))
                    .build(ComponentManager.clearAndGetScene());

            newLabel(CENTER)
                    .gridLayoutData(GridData.FILL_HORIZONTAL)
                    .text(FileUtils.getRelativePath(file).toString())
                    .bold()
                    .build(topComposite);

            final Composite textComposite = compositeBuilder
                    .layout(gridLayout().margins(0).marginLeft(5))
                    .build(topComposite);

            newText(MULTI | READ_ONLY | WRAP | V_SCROLL)
                    .gridLayoutData(GridData.FILL_BOTH)
                    .text(content)
                    .requestLayout()
                    .build(textComposite);

        } catch (IOException exception) {
            Message.error("An error occurred while reading the file", exception);
        }
    }
}
