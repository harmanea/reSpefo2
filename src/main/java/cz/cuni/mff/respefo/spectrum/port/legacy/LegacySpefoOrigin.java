package cz.cuni.mff.respefo.spectrum.port.legacy;

import cz.cuni.mff.respefo.spectrum.origin.BaseOrigin;
import cz.cuni.mff.respefo.spectrum.origin.Origin;

import java.util.Arrays;

@Origin(key = "legacy")
public class LegacySpefoOrigin extends BaseOrigin {

    private String remark;
    private String usedCal;
    private short starStep;
    private double[] dispCoef;
    private double minTransp;
    private double maxInt;
    private double[] filterWidth;
    private int reserve;

    private LegacySpefoOrigin() {
        // default empty constructor
    }

    public LegacySpefoOrigin(String fileName) {
        super(fileName);
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getUsedCal() {
        return usedCal;
    }

    public void setUsedCal(String usedCal) {
        this.usedCal = usedCal;
    }

    public short getStarStep() {
        return starStep;
    }

    public void setStarStep(short starStep) {
        this.starStep = starStep;
    }

    public double getDispCoef(int index) {
        return dispCoef[index];
    }

    public void setDispCoef(double[] dispCoef) {
        this.dispCoef = dispCoef;
    }

    public double getMinTransp() {
        return minTransp;
    }

    public void setMinTransp(double minTransp) {
        this.minTransp = minTransp;
    }

    public double getMaxInt() {
        return maxInt;
    }

    public void setMaxInt(double maxInt) {
        this.maxInt = maxInt;
    }

    public boolean hasValidFilterWidth() {
        return Arrays.stream(filterWidth).anyMatch(d -> d != 0);
    }

    public double getFilterWidth(int index) {
        return filterWidth[index];
    }

    public void setFilterWidth(double[] filterWidth) {
        this.filterWidth = filterWidth;
    }

    public int getReserve() {
        return reserve;
    }

    public void setReserve(int reserve) {
        this.reserve = reserve;
    }
}
