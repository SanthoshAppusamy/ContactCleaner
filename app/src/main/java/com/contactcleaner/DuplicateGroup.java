package com.contactcleaner;

import java.util.List;

public class DuplicateGroup {
    private String normalizedNumber;
    private List<ContactItem> contacts;

    public DuplicateGroup(String normalizedNumber, List<ContactItem> contacts) {
        this.normalizedNumber = normalizedNumber;
        this.contacts = contacts;
    }

    public String getNormalizedNumber() { return normalizedNumber; }
    public List<ContactItem> getContacts() { return contacts; }

    public int getSelectedCount() {
        int count = 0;
        for (ContactItem c : contacts) { if (c.isSelected()) count++; }
        return count;
    }
}
