package cz.cuni.mff.respefo.function.lst;

import cz.cuni.mff.respefo.component.FileExplorer;
import cz.cuni.mff.respefo.component.Project;
import cz.cuni.mff.respefo.function.MultiFileFunction;
import cz.cuni.mff.respefo.logging.Log;
import cz.cuni.mff.respefo.util.Message;
import cz.cuni.mff.respefo.util.Progress;
import cz.cuni.mff.respefo.util.collections.JulianDate;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static cz.cuni.mff.respefo.function.lst.LstFile.DATE_TIME_FORMATTER;
import static cz.cuni.mff.respefo.util.utils.FormattingUtils.formatDouble;
import static cz.cuni.mff.respefo.util.utils.FormattingUtils.formatInteger;

public abstract class AbstractLstFunction<T> implements MultiFileFunction {

    protected static final String TABLE_HEADER =
            "==============================================================================\n" +
            "   N.  Date & UT start       exp[s]      Filename       J.D.hel.  RVcorr\n" +
            "==============================================================================\n";

    @Override
    public void execute(List<File> files) {
        Progress.withProgressTracking(p -> {
            p.refresh("Opening files", files.size());

            List<T> ts = new ArrayList<>();
            for (File file : files) {
                try {
                    ts.add(openFile(file));
                } catch (Exception exception) {
                    Log.error("An error occurred while opening file " + file.getPath(), exception);
                } finally {
                    p.step();
                }
            }

            return ts;
        }, meta -> {
            String fileName = Project.getRootFileName(".lst");
            try (PrintWriter writer = new PrintWriter(fileName)) {
                writer.print("\n\n\n\n"); // TODO: generate some relevant header
                writer.print(TABLE_HEADER);

                for (int i = 0; i < meta.size(); i++) {
                    T t = meta.get(i);

                    writer.println(String.join(" ",
                            formatInteger(i + 1, 5),
                            getDateOfObservation(t).format(DATE_TIME_FORMATTER),
                            formatDouble(getExpTime(t), 5, 3, false),
                            getFile(t).getName(),
                            formatDouble(getHJD(t).getRJD(), 5, 4),
                            formatDouble(getRvCorrection(t), 3, 2)));
                }

                FileExplorer.getDefault().refresh();
                Message.info("File created successfully");

            } catch (IOException exception) {
                Message.error("Couldn't generate .lst file", exception);
            }
        });
    }

    protected abstract T openFile(File file) throws Exception;

    protected abstract LocalDateTime getDateOfObservation(T t);

    protected abstract File getFile(T t);

    protected abstract double getExpTime(T t);

    protected abstract JulianDate getHJD(T t);

    protected abstract double getRvCorrection(T t);
}
