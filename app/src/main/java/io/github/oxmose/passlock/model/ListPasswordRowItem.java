package io.github.oxmose.passlock.model;

public class ListPasswordRowItem {




    public enum ITEM_TYPE {
        PASSWORD, PIN, DIGICODE
    }

    private String title;
    private String value;

    private ITEM_TYPE type;

    private int id;

    public ListPasswordRowItem(String title, String value, ITEM_TYPE type, int id) {
        this.title = title;
        this.value = value;
        this.type = type;
        this.id = id;
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

    public int getId() { return this.id; }

    public void setType(ITEM_TYPE type) {
        this.type = type;
    }
}
