package cz.cuni.mff.respefo.function.prepare;

import cz.cuni.mff.respefo.dialog.TitleAreaDialog;
import cz.cuni.mff.respefo.resources.ImageResource;
import cz.cuni.mff.respefo.util.FileDialogs;
import cz.cuni.mff.respefo.util.FileType;
import cz.cuni.mff.respefo.util.widget.DefaultSelectionListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;

import static cz.cuni.mff.respefo.util.layout.GridDataBuilder.gridData;
import static cz.cuni.mff.respefo.util.layout.GridLayoutBuilder.gridLayout;
import static cz.cuni.mff.respefo.util.widget.ButtonBuilder.newButton;
import static cz.cuni.mff.respefo.util.widget.CompositeBuilder.newComposite;
import static cz.cuni.mff.respefo.util.widget.LabelBuilder.newLabel;
import static cz.cuni.mff.respefo.util.widget.TextBuilder.newText;

public class PrepareProjectDialog extends TitleAreaDialog {

    public static final int HEC2 = -1;
    public static final int NEW_LST = -2;
    public static final int USE_LST = -3;

    private int status;

    private String prefix;
    private String lstFileName;

    protected PrepareProjectDialog(String suggestedPrefix, boolean useLst, String lstFileName) {
        super("Prepare project");

        this.prefix = suggestedPrefix;
        this.lstFileName = lstFileName;

        status = useLst ? USE_LST : HEC2;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getLstFileName() {
        return lstFileName;
    }

    @Override
    protected void createDialogArea(Composite parent) {
        setMessage("Select a prefix for the project", SWT.ICON_INFORMATION);

        final Composite composite = newComposite()
                .layout(gridLayout(2, false).margins(15).verticalSpacing(20).horizontalSpacing(10))
                .layoutData(gridData(GridData.FILL_BOTH).widthHint(450).build())
                .build(parent);

        newLabel().text("Prefix:").gridLayoutData(GridData.HORIZONTAL_ALIGN_BEGINNING).build(composite);

        newText(SWT.SINGLE | SWT.BORDER)
                .text(prefix)
                .gridLayoutData(GridData.FILL_HORIZONTAL)
                .onModify(event -> {
                    prefix = ((Text) event.widget).getText();
                    if (prefix.length() == 0) {
                        setMessage("The prefix cannot be blank", SWT.ICON_WARNING);
                        getButton(SWT.OK).setEnabled(false);
                    } else {
                        if (prefix.length() == 3) {
                            setMessage("Select a prefix for the project", SWT.ICON_INFORMATION);
                        } else {
                            setMessage("The recommended prefix length is three", SWT.ICON_WARNING);
                        }
                        getButton(SWT.OK).setEnabled(true);
                    }
                })
                .selectText()
                .build(composite);

        final Group group = new Group(composite, SWT.NONE);
        group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
        group.setLayout(gridLayout().verticalSpacing(10).build());
        group.setText("Mode");

        newButton(SWT.RADIO)
                .text("Generate hec2 input data")
                .gridLayoutData(GridData.FILL_HORIZONTAL)
                .selection(status == HEC2)
                .onSelection(event -> status = HEC2)
                .build(group);

        newButton(SWT.RADIO)
                .text("Generate .lst file")
                .gridLayoutData(GridData.FILL_HORIZONTAL)
                .onSelection(event -> status = NEW_LST)
                .build(group);

        final Button thirdButton = newButton(SWT.RADIO)
                .gridLayoutData(GridData.FILL_HORIZONTAL)
                .selection(status == USE_LST)
                .onSelection(event -> status = USE_LST)
                .build(group);

        final Composite lstFileComposite = newComposite()
                .layout(gridLayout(2, false).marginLeft(thirdButton.computeSize(SWT.DEFAULT, SWT.DEFAULT).x).marginHeight(0))
                .gridLayoutData(GridData.FILL_HORIZONTAL)
                .build(group);

        thirdButton.setText("Use existing .lst file");

        final Text lstFileNameText = newText(SWT.SINGLE | SWT.BORDER)
                .text(lstFileName)
                .gridLayoutData(GridData.FILL_BOTH)
                .enabled(status == USE_LST)
                .onModify(event -> lstFileName = ((Text) event.widget).getText())
                .build(lstFileComposite);

        final Button lstButton = newButton(SWT.PUSH)
                .image(ImageResource.FOLDER)
                .gridLayoutData(GridData.HORIZONTAL_ALIGN_END)
                .enabled(status == USE_LST)
                .onSelection(event -> {
                    String fileName = FileDialogs.openFileDialog(FileType.LST);
                    if (fileName != null) {
                        lstFileNameText.setText(fileName);
                    }
                })
                .build(lstFileComposite);

        thirdButton.addSelectionListener(new DefaultSelectionListener(event -> {
            lstFileNameText.setEnabled(thirdButton.getSelection());
            lstButton.setEnabled(thirdButton.getSelection());
        }));
    }

    @Override
    protected void buttonPressed(int returnCode) {
        if (returnCode == SWT.OK) {
            returnCode = status;
        }

        super.buttonPressed(returnCode);
    }
}
