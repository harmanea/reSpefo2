package cz.cuni.mff.respefo.util.utils;

import org.junit.Test;

import static cz.cuni.mff.respefo.util.TestUtils.buildPath;
import static org.junit.Assert.assertEquals;

public class FileUtilsTest {
    @Test
    public void testFileExtensionUtils() {
        String fileName = buildPath("foo", "bar", "expected.extension");
        assertEquals("extension", FileUtils.getFileExtension(fileName));
        assertEquals(buildPath("foo", "bar", "expected"), FileUtils.stripFileExtension(fileName));
        assertEquals(buildPath("foo", "bar", "expected.newextension"), FileUtils.replaceFileExtension(fileName, "newextension"));

        fileName = "noextension";
        assertEquals("", FileUtils.getFileExtension(fileName));
        assertEquals(fileName, FileUtils.stripFileExtension(fileName));
        assertEquals(fileName + ".newextension", FileUtils.replaceFileExtension(fileName, "newextension"));

        fileName = "more.than.one.extension";
        assertEquals("extension", FileUtils.getFileExtension(fileName));
        assertEquals("more.than.one", FileUtils.stripFileExtension(fileName));
    }

    @Test
    public void testParentDirectoryUtils() {
        String path = buildPath("", "user", "home", "file.txt");
        assertEquals( buildPath("", "user", "home"), FileUtils.getParentDirectory(path));
        assertEquals("file.txt", FileUtils.stripParent(path));

        path = "file.txt";
        assertEquals("", FileUtils.getParentDirectory(path));
        assertEquals("file.txt", FileUtils.stripParent(path));
    }
}