package com.contactcleaner;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DuplicateListActivity extends AppCompatActivity {
    public static List<DuplicateGroup> duplicateGroups;
    private RecyclerView recyclerView;
    private Button btnDelete;
    private TextView tvGroupCount;
    private List<Object> flatItems = new ArrayList<>();
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_duplicate_list);
        recyclerView = findViewById(R.id.recyclerView);
        btnDelete = findViewById(R.id.btnDeleteSelected);
        tvGroupCount = findViewById(R.id.tvGroupCount);
        if (duplicateGroups == null || duplicateGroups.isEmpty()) {
            Toast.makeText(this, "No duplicates to show.", Toast.LENGTH_SHORT).show();
            finish(); return;
        }
        tvGroupCount.setText(duplicateGroups.size() + " duplicate group(s) found");
        for (DuplicateGroup g : duplicateGroups) {
            flatItems.add(g);
            flatItems.addAll(g.getContacts());
        }
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new DuplicateGroupAdapter(flatItems));
        btnDelete.setOnClickListener(v -> confirmAndDelete());
    }

    private List<ContactItem> getSelected() {
        List<ContactItem> sel = new ArrayList<>();
        for (Object o : flatItems) { if (o instanceof ContactItem && ((ContactItem)o).isSelected()) sel.add((ContactItem)o); }
        return sel;
    }

    private void confirmAndDelete() {
        List<ContactItem> selected = getSelected();
        if (selected.isEmpty()) { Toast.makeText(this, "Select at least one contact to delete.", Toast.LENGTH_SHORT).show(); return; }
        new AlertDialog.Builder(this)
            .setTitle("Confirm Deletion")
            .setMessage("Permanently delete " + selected.size() + " contact(s)? This cannot be undone.")
            .setPositiveButton("Delete", (d, w) -> {
                btnDelete.setEnabled(false);
                executor.execute(() -> {
                    int count = ContactHelper.deleteContacts(getContentResolver(), selected);
                    mainHandler.post(() -> {
                        btnDelete.setEnabled(true);
                        new AlertDialog.Builder(this)
                            .setTitle("Done!")
                            .setMessage(count + " duplicate contact(s) removed successfully.")
                            .setPositiveButton("OK", (dd, ww) -> { duplicateGroups = null; finish(); })
                            .setCancelable(false).show();
                    });
                });
            })
            .setNegativeButton("Cancel", null).show();
    }

    @Override protected void onDestroy() { super.onDestroy(); executor.shutdown(); }
}
