package com.contactcleaner;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContactHelper {

    public static String normalizeNumber(String number) {
        if (number == null) return "";
        String digits = number.replaceAll("[^0-9]", "");
        while (digits.startsWith("0") && digits.length() > 10) {
            digits = digits.substring(1);
        }
        if (digits.length() > 10) {
            if (digits.startsWith("91") && digits.length() == 12) {
                digits = digits.substring(2);
            } else if (digits.startsWith("1") && digits.length() == 11) {
                digits = digits.substring(1);
            } else if (digits.length() > 10) {
                digits = digits.substring(digits.length() - 10);
            }
        }
        return digits;
    }

    public static List<DuplicateGroup> findDuplicates(ContentResolver resolver) {
        Map<String, List<ContactItem>> numberMap = new HashMap<>();
        String[] projection = new String[]{
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER
        };
        Cursor cursor = resolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                projection, null, null,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
        );
        if (cursor == null) return new ArrayList<>();
        Map<String, String> seen = new HashMap<>();
        while (cursor.moveToNext()) {
            String id = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.CONTACT_ID));
            String name = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String rawNumber = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER));
            if (rawNumber == null || rawNumber.trim().isEmpty()) continue;
            String normalized = normalizeNumber(rawNumber);
            if (normalized.length() < 7) continue;
            String key = normalized + "|" + id;
            if (seen.containsKey(key)) continue;
            seen.put(key, id);
            ContactItem item = new ContactItem(id, name != null ? name : "Unknown", rawNumber, normalized);
            List<ContactItem> group = numberMap.get(normalized);
            if (group == null) { group = new ArrayList<>(); numberMap.put(normalized, group); }
            boolean alreadyIn = false;
            for (ContactItem e : group) { if (e.getId().equals(id)) { alreadyIn = true; break; } }
            if (!alreadyIn) group.add(item);
        }
        cursor.close();
        List<DuplicateGroup> duplicates = new ArrayList<>();
        for (Map.Entry<String, List<ContactItem>> entry : numberMap.entrySet()) {
            if (entry.getValue().size() >= 2) {
                duplicates.add(new DuplicateGroup(entry.getKey(), entry.getValue()));
            }
        }
        return duplicates;
    }

    public static int deleteContacts(ContentResolver resolver, List<ContactItem> toDelete) {
        int deleted = 0;
        for (ContactItem contact : toDelete) {
            Uri uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, Long.parseLong(contact.getId()));
            if (resolver.delete(uri, null, null) > 0) deleted++;
        }
        return deleted;
    }
}
