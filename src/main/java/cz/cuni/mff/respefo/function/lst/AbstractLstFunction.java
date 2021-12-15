package cz.cuni.mff.respefo.function.lst;

import cz.cuni.mff.respefo.component.Project;
import cz.cuni.mff.respefo.exception.SpefoException;
import cz.cuni.mff.respefo.function.MultiFileFunction;
import cz.cuni.mff.respefo.logging.Log;
import cz.cuni.mff.respefo.util.Message;
import cz.cuni.mff.respefo.util.Progress;
import cz.cuni.mff.respefo.util.collections.JulianDate;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractLstFunction<T> implements MultiFileFunction {

    @Override
    public void execute(List<File> files) {
        Progress.withProgressTracking(p -> {
            p.refresh("Opening files", files.size());

            List<T> ts = new ArrayList<>();
            for (File file : files) {
                try {
                    ts.add(openFile(file));
                } catch (SpefoException exception) {
                    Log.error("An error occurred while opening file " + file.getPath(), exception);
                } finally {
                    p.step();
                }
            }

            return ts;
        }, meta -> {
            String fileName = Project.getRootFileName(".lst");
            LstFile lstFile = new LstFile(""); // TODO: generate some relevant header
            for (int i = 0; i < meta.size(); i++) {
                T t = meta.get(i);
                lstFile.addRecord(new LstFile.Record(i + 1,
                        getDateOfObservation(t),
                        getExpTime(t),
                        getFile(t).getName(),
                        getHJD(t),
                        getRvCorrection(t)));
            }
            try {
                lstFile.saveAs(new File(fileName));
            } catch (SpefoException exception) {
                Message.error("Couldn't generate .lst file", exception);
            }
        });
    }

    protected abstract T openFile(File file) throws SpefoException;

    protected abstract LocalDateTime getDateOfObservation(T t);

    protected abstract File getFile(T t);

    protected abstract double getExpTime(T t);

    protected abstract JulianDate getHJD(T t);

    protected abstract double getRvCorrection(T t);
}
