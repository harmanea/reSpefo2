package cz.cuni.mff.respefo.spectrum.port.legacy;

import cz.cuni.mff.respefo.exception.InvalidFileFormatException;
import cz.cuni.mff.respefo.util.collections.DoubleArrayList;

import java.io.FileInputStream;
import java.io.IOException;

import static cz.cuni.mff.respefo.util.utils.PascalUtils.bytesToInt;
import static cz.cuni.mff.respefo.util.utils.PascalUtils.bytesToShort;

public class LegacyConFile {
    private static final char SQUARECHAR = 254;

    protected String remark;
    protected int[] rectX;
    protected DoubleArrayList rectY;

    protected LegacyConFile() {}

    public LegacyConFile(String fileName) throws InvalidFileFormatException {
        try (FileInputStream inputStream = new FileInputStream(fileName)) {
            byte[] bytes = readBytes(inputStream,2);
            boolean extended = (bytes[1] & 0x00FF) == SQUARECHAR;

            int maxPoints = extended ? readShort(inputStream) : 100;

            if (extended) {
                readBytes(inputStream, 12);
                remark = readString(inputStream, 30);
            } else {
                remark = new String(bytes) + readString(inputStream, 28);
            }

            short rectNum = readShort(inputStream);

            rectX = new int[rectNum];
            rectY = new DoubleArrayList(rectNum);

            for (int i = 0; i < rectNum; i++) {
                rectX[i] = readInt(inputStream);
            }

            readBytes(inputStream, (maxPoints - rectNum) * 4); // skip empty space

            for (int i = 0; i < rectNum; i++) {
                rectY.add(readShort(inputStream));
            }

        } catch (IOException exception) {
            throw new InvalidFileFormatException("Con file has invalid format");
        }
    }

    protected byte[] readBytes(FileInputStream inputStream, int numberOfBytes) throws IOException, InvalidFileFormatException {
        byte[] bytes = new byte[numberOfBytes];

        int res = inputStream.read(bytes);
        if (res < numberOfBytes) {
            throw new InvalidFileFormatException("The file is too short");
        }

        return bytes;
    }

    protected int readInt(FileInputStream inputStream) throws IOException, InvalidFileFormatException {
        return bytesToInt(readBytes(inputStream, 4));
    }

    protected short readShort(FileInputStream inputStream) throws IOException, InvalidFileFormatException {
        return bytesToShort(readBytes(inputStream, 2));
    }

    protected String readString(FileInputStream inputStream, int length) throws IOException, InvalidFileFormatException {
        return new String(readBytes(inputStream, length)).replace("\00", "").trim();
    }

    public String getRemark() {
        return remark;
    }

    public int[] getRectX() {
        return rectX;
    }

    public DoubleArrayList getRectY() {
        return rectY;
    }
}
