package cz.cuni.mff.respefo.util;

import org.junit.Test;

import java.io.File;

import static org.junit.Assert.*;

public class FileUtilsTest {
    @Test
    public void testFileExtensionUtils() {
        String fileName = "foo" + File.separator + "bar" + File.separator + "expected.extension";
        assertEquals("extension", FileUtils.getFileExtension(fileName));
        assertEquals("foo" + File.separator + "bar" + File.separator + "expected", FileUtils.stripFileExtension(fileName));

        fileName = "noExtension";
        assertEquals("", FileUtils.getFileExtension(fileName));
        assertEquals(fileName, FileUtils.stripFileExtension(fileName));

        fileName = "more.than.one.extension";
        assertEquals("extension", FileUtils.getFileExtension(fileName));
        assertEquals("more.than.one", FileUtils.stripFileExtension(fileName));
    }

    @Test
    public void testParentDirectoryUtils() {
        String path = File.separator + "adam" + File.separator + "home" + File.separator + "bla.txt";

        assertEquals( File.separator + "adam" + File.separator + "home", FileUtils.getParentDirectory(path));
        assertEquals("bla.txt", FileUtils.stripParent(path));

        path = "bla.txt";

        assertEquals("", FileUtils.getParentDirectory(path));
        assertEquals("bla.txt", FileUtils.stripParent(path));
    }
}