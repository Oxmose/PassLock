package io.github.oxmose.passlock.model;

public class ListPasswordRowItem {



    public enum ITEM_TYPE {
        PASSWORD, PIN, DIGICODE
    }

    String title;
    String value;

    ITEM_TYPE type;

    public ListPasswordRowItem(String title, String value, ITEM_TYPE type) {
        this.title = title;
        this.value = value;
        this.type=type;
    }

    public String getTitle() {
        return title;
    }

    public String getValue() {
        return value;
    }

    public ITEM_TYPE getType() {
        return type;
    }

    public void setType(ITEM_TYPE type) {
        this.type = type;
    }
}
