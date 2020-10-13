package cz.cuni.mff.respefo.format.formats.fits;

public class HeaderCard {
    private String comment;
    private String key;
    private String value;

    private HeaderCard() {
        // default empty constructor
    }

    public HeaderCard(String comment, String key, String value) {
        this.comment = comment;
        this.key = key;
        this.value = value;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
