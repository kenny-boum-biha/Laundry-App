package com.example.laundryapp;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;

public class RoomActivity extends AppCompatActivity {

    private static final String TAG = "RoomActivity";
    private static final String CHANNEL_ID = "laundry_updates_channel";
    private static final int PERMISSION_REQUEST_CODE = 101;

    // UI Components
    private RecyclerView recyclerMachines;
    private TextView textNoReservations;
    private Button buttonFilterAll, buttonFilterRunning, buttonFilterIdle, buttonFilterFinished;

    // Data & Adapters
    private MachineAdapter machineAdapter;
    private ArrayList<MachineItem> machines; // Full list
    private ArrayList<MachineItem> filteredMachines; // Displayed list
    private String currentFilter = "all";

    // Firebase & Services
    private FirebaseFirestore firestore;
    private ListenerRegistration machinesListener;
    private BluetoothServices bluetoothService;

    // IDs
    private String roomId;
    private String locationId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);

        // 1. Request Notification Permissions (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, PERMISSION_REQUEST_CODE);
            }
        }
        createNotificationChannel();

        // 2. Retrieve Intent Data
        roomId = getIntent().getStringExtra("roomId");
        locationId = getIntent().getStringExtra("locationID"); // Standardized to look for "locationID"
        String roomName = getIntent().getStringExtra("roomTitle");
        if (roomName == null) roomName = "Laundry Room";

        // 3. Setup Toolbar
        Toolbar toolbar = findViewById(R.id.roomToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(roomName);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // 4. Initialize UI
        recyclerMachines = findViewById(R.id.recyclerMachines);
        textNoReservations = findViewById(R.id.textNoReservations);
        buttonFilterAll = findViewById(R.id.buttonFilterAll);
        buttonFilterRunning = findViewById(R.id.buttonFilterRunning);
        buttonFilterIdle = findViewById(R.id.buttonFilterIdle);
        buttonFilterFinished = findViewById(R.id.buttonFilterFinished);

        // 5. Setup RecyclerView
        recyclerMachines.setLayoutManager(new GridLayoutManager(this, 2));
        machines = new ArrayList<>();
        filteredMachines = new ArrayList<>();
        // Pass locationId and roomId to adapter in case it needs them for sub-queries
        machineAdapter = new MachineAdapter(filteredMachines, locationId, roomId);
        recyclerMachines.setAdapter(machineAdapter);

        // 6. Firebase Init
        firestore = FirebaseFirestore.getInstance();

        // 7. Setup Filters & Listeners
        setupFilterButtons();
        updateFilterButtonStates();

        // Start listening for DB changes
        listenToMachineUpdates();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Start simulated BluetoothService
        bluetoothService = new BluetoothServices(5000L);
        // Note: We will start reading specific machines inside the Firestore listener
        // once we actually know which machines exist.
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (bluetoothService != null) {
            bluetoothService.stopReading();
            bluetoothService = null;
        }
        if (machinesListener != null) {
            machinesListener.remove();
            machinesListener = null;
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    // ============================================================
    //                   FIRESTORE LISTENER
    // ============================================================

    private void listenToMachineUpdates() {
        if (locationId == null || roomId == null) {
            Log.e(TAG, "ERROR: LocationID or RoomID is NULL.");
            return;
        }

        machinesListener = firestore.collection("locations")
                .document(locationId)
                .collection("rooms")
                .document(roomId)
                .collection("machines")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Firestore Listen Failed", error);
                        return;
                    }

                    if (value != null) {
                        boolean hasChanges = false;

                        for (DocumentChange dc : value.getDocumentChanges()) {
                            try {
                                MachineItem machine = dc.getDocument().toObject(MachineItem.class);
                                // If manual parsing is needed because your DB structure is complex (like the 'telemetry' map in Code B),
                                // you would do it here. For now, assuming toObject works as per Code A.

                                switch (dc.getType()) {
                                    case ADDED:
                                        machines.add(machine);
                                        if (bluetoothService != null) {
                                            bluetoothService.startReading(roomId, machine.name.replaceAll("\\s+", "_"));
                                        }
                                        hasChanges = true;
                                        break;

                                    case MODIFIED:
                                        for (int i = 0; i < machines.size(); i++) {
                                            MachineItem existing = machines.get(i);
                                            if (existing.id != null && existing.id.equals(machine.id)) {

                                                // CHECK FOR NOTIFICATION CONDITION
                                                // If it wasn't finished before, but is finished now -> Notify
                                                if (!isFinished(existing.status) && isFinished(machine.status)) {
                                                    displayCompletionNotification("Laundry Update", machine.name + " has finished!");
                                                }

                                                machines.set(i, machine);
                                                hasChanges = true;
                                                break;
                                            }
                                        }
                                        break;

                                    case REMOVED:
                                        for (int i = 0; i < machines.size(); i++) {
                                            if (machines.get(i).id != null && machines.get(i).id.equals(machine.id)) {
                                                machines.remove(i);
                                                hasChanges = true;
                                                break;
                                            }
                                        }
                                        break;
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error parsing machine doc: " + dc.getDocument().getId(), e);
                            }
                        }

                        if (hasChanges) {
                            applyFilter(currentFilter);
                        }
                    }
                });
    }

    // Helper to determine if a status string means "finished"
    private boolean isFinished(String status) {
        return status != null && (status.equalsIgnoreCase("finished") || status.equalsIgnoreCase("done"));
    }

    // ============================================================
    //                   NOTIFICATIONS
    // ============================================================

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Machine Updates",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notifies when machine has completed its task");
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    private void displayCompletionNotification(String title, String message) {
        // Check permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                return; // Cannot show notification without permission
            }
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground) // Make sure this icon exists, or change to R.mipmap.ic_launcher
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManagerCompat manager = NotificationManagerCompat.from(this);
        manager.notify((int) System.currentTimeMillis(), builder.build());
    }

    // ============================================================
    //                   FILTERING LOGIC
    // ============================================================

    private void setupFilterButtons() {
        buttonFilterAll.setOnClickListener(v -> applyFilter("all"));
        buttonFilterRunning.setOnClickListener(v -> applyFilter("running"));
        buttonFilterIdle.setOnClickListener(v -> applyFilter("idle"));
        buttonFilterFinished.setOnClickListener(v -> applyFilter("finished"));
    }

    private void applyFilter(String filter) {
        currentFilter = filter;
        filteredMachines.clear();

        for (MachineItem machine : machines) {
            String status = machine.status != null && !machine.status.isEmpty()
                    ? machine.status.toLowerCase() : "idle";

            boolean shouldInclude = false;
            switch (filter) {
                case "all": shouldInclude = true; break;
                case "running": shouldInclude = status.equals("running"); break;
                case "idle": shouldInclude = status.equals("idle"); break;
                case "finished": shouldInclude = isFinished(status); break;
            }

            if (shouldInclude) {
                filteredMachines.add(machine);
            }
        }

        updateFilterButtonStates();
        machineAdapter.notifyDataSetChanged();

        // Handle Empty State Text
        if (filteredMachines.isEmpty()) {
            textNoReservations.setVisibility(View.VISIBLE);
            String msg = "No machines found";
            if (filter.equals("idle")) msg = "No machines idle";
            else if (filter.equals("running")) msg = "No machines running";
            else if (filter.equals("finished")) msg = "No machines finished";
            textNoReservations.setText(msg);
        } else {
            textNoReservations.setVisibility(View.GONE);
        }
    }

    private void updateFilterButtonStates() {
        int activeColor = 0xFF6200EE; // Purple
        int inactiveColor = 0xFF757575; // Gray

        setButtonColor(buttonFilterAll, currentFilter.equals("all") ? activeColor : inactiveColor);
        setButtonColor(buttonFilterRunning, currentFilter.equals("running") ? activeColor : inactiveColor);
        setButtonColor(buttonFilterIdle, currentFilter.equals("idle") ? activeColor : inactiveColor);
        setButtonColor(buttonFilterFinished, currentFilter.equals("finished") ? activeColor : inactiveColor);
    }

    private void setButtonColor(Button btn, int color) {
        btn.setBackgroundTintList(ColorStateList.valueOf(color));
    }
}