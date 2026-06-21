package com.contactcleaner;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CODE = 100;
    private Button btnScan;
    private ProgressBar progressBar;
    private TextView tvStatus;
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnScan = findViewById(R.id.btnScan);
        progressBar = findViewById(R.id.progressBar);
        tvStatus = findViewById(R.id.tvStatus);
        btnScan.setOnClickListener(v -> checkPermissionsAndScan());
    }

    private void checkPermissionsAndScan() {
        boolean read = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED;
        boolean write = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CONTACTS) == PackageManager.PERMISSION_GRANTED;
        if (read && write) { startScan(); }
        else {
            ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS},
                PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allGranted = true;
            for (int r : grantResults) { if (r != PackageManager.PERMISSION_GRANTED) { allGranted = false; break; } }
            if (allGranted) startScan();
            else Toast.makeText(this, "Contacts permission is required.", Toast.LENGTH_LONG).show();
        }
    }

    private void startScan() {
        btnScan.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);
        tvStatus.setText("Scanning contacts...");
        tvStatus.setVisibility(View.VISIBLE);
        executor.execute(() -> {
            List<DuplicateGroup> duplicates = ContactHelper.findDuplicates(getContentResolver());
            mainHandler.post(() -> {
                progressBar.setVisibility(View.GONE);
                btnScan.setEnabled(true);
                if (duplicates.isEmpty()) {
                    tvStatus.setText("No duplicate contacts found. Your contacts are clean!");
                } else {
                    tvStatus.setText("Found " + duplicates.size() + " group(s) of duplicates.");
                    DuplicateListActivity.duplicateGroups = duplicates;
                    startActivity(new Intent(MainActivity.this, DuplicateListActivity.class));
                }
            });
        });
    }

    @Override protected void onDestroy() { super.onDestroy(); executor.shutdown(); }
}
