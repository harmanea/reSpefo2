package cz.cuni.mff.respefo.function.scan;

import cz.cuni.mff.respefo.function.Fun;
import cz.cuni.mff.respefo.function.SingleFileFunction;
import cz.cuni.mff.respefo.util.Message;
import cz.cuni.mff.respefo.util.Progress;

import java.io.File;

@Fun(name = "Progress Bar Test")
public class TestFunction implements SingleFileFunction {
    @Override
    public void execute(File file) {
        Progress.withProgressTracking(progress -> {
            progress.refresh("Running background test", 10);

            for (int i = 0; i < 10; i++) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                progress.step();
            }
            return "Done";
        }, Message::info);
    }
}
