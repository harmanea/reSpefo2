package cz.cuni.mff.respefo.format.formats.legacy;

import cz.cuni.mff.respefo.format.InvalidFileFormatException;
import cz.cuni.mff.respefo.util.collections.DoubleArrayList;

import java.io.FileInputStream;
import java.io.IOException;

import static cz.cuni.mff.respefo.util.Constants.SPEED_OF_LIGHT;
import static cz.cuni.mff.respefo.util.utils.PascalUtils.pascalExtendedToDouble;
import static cz.cuni.mff.respefo.util.utils.PascalUtils.pascalRealToDouble;

public class LegacySpefoFile extends LegacyConFile {

    private final String usedCal;
    private final short starStep;
    private final double[] dispCoef;
    private final double minTransp;
    private final double maxInt;
    private final double[] filterWidth;
    private final int reserve;
    private final double rvCorr;

    private double[] ySeries;

    public LegacySpefoFile(String fileName) throws InvalidFileFormatException {
        try (FileInputStream inputStream = new FileInputStream(fileName)) {
            remark = readString(inputStream, 30);
            usedCal = readString(inputStream, 8);

            starStep = readShort(inputStream);

            dispCoef = new double[7];
            for (int i = 0; i < 7; i++) {
                dispCoef[i] = readPascalExtended(inputStream);
                if (Double.isInfinite(dispCoef[i])) {
                    dispCoef[i] = 0;
                }
            }

            rvCorr = (dispCoef[6] - 1) * SPEED_OF_LIGHT;

            minTransp = readPascalExtended(inputStream);
            maxInt = readPascalExtended(inputStream);

            filterWidth = new double[4];
            for (int i = 0; i < 4; i++) {
                filterWidth[i] = readPascalReal(inputStream);
            }

            reserve = readInt(inputStream);

            short rectNum = readShort(inputStream);
            if (rectNum < 0) {
                rectNum = (short) -rectNum;
            }

            rectX = new int[rectNum];
            for (int i = 0; i < rectNum; i++) {
                rectX[i] = readInt(inputStream);
            }
            readBytes(inputStream, 160 - rectNum * 4);  // skip empty space

            rectY = new DoubleArrayList(rectNum);
            for (int i = 0; i < rectNum; i++) {
                rectY.add(readShort(inputStream));
            }
            readBytes(inputStream, 80 - rectNum * 2);  // skip empty space

            DoubleArrayList yList = new DoubleArrayList();
            while (inputStream.available() > 0) {
                short value = readShort(inputStream);
                yList.add(value);
            }
            ySeries = yList.toArray();

        } catch (IOException exception) {
            throw new InvalidFileFormatException("File has invalid format");
        }
    }

    private double readPascalExtended(FileInputStream inputStream) throws IOException, InvalidFileFormatException {
        return pascalExtendedToDouble(readBytes(inputStream, 10));
    }

    private double readPascalReal(FileInputStream inputStream) throws IOException, InvalidFileFormatException {
        return pascalRealToDouble(readBytes(inputStream, 6));
    }

    public String getUsedCal() {
        return usedCal;
    }

    public short getStarStep() {
        return starStep;
    }

    public double[] getDispCoef() {
        return dispCoef;
    }

    public double getMinTransp() {
        return minTransp;
    }

    public double getMaxInt() {
        return maxInt;
    }

    public double[] getFilterWidth() {
        return filterWidth;
    }

    public int getReserve() {
        return reserve;
    }

    public double getRvCorr() {
        return rvCorr;
    }

    public double[] getYSeries() {
        return ySeries;
    }

    public void setYSeries(double[] ySeries) {
        this.ySeries = ySeries;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public void setRectX(int[] rectX) {
        this.rectX = rectX;
    }

    public void setRectY(DoubleArrayList rectY) {
        this.rectY = rectY;
    }

    public boolean isRectified() {
        return rectX.length > 0;
    }
}
