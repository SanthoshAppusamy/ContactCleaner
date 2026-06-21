package com.contactcleaner;

public class ContactItem {
    private String id;
    private String name;
    private String rawNumber;
    private String normalizedNumber;
    private boolean selected;

    public ContactItem(String id, String name, String rawNumber, String normalizedNumber) {
        this.id = id;
        this.name = name;
        this.rawNumber = rawNumber;
        this.normalizedNumber = normalizedNumber;
        this.selected = false;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getRawNumber() { return rawNumber; }
    public String getNormalizedNumber() { return normalizedNumber; }
    public boolean isSelected() { return selected; }
    public void setSelected(boolean selected) { this.selected = selected; }
}
