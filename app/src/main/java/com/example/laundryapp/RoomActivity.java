package com.example.laundryapp;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import androidx.annotation.Nullable;

public class RoomActivity extends AppCompatActivity {

    private RecyclerView recyclerMachines;
    private TextView textNoReservations;
    private MachineAdapter machineAdapter;
    private ArrayList<MachineItem> machines; // Full list of all machines
    private ArrayList<MachineItem> filteredMachines; // Filtered list shown in RecyclerView
    private BluetoothServices bluetoothService;
    private FirebaseFirestore firestore;

    private String roomId;
    private String currentFilter = "all"; // "all", "running", "idle", "finished"
    private String locationID;

    // Filter buttons
    private Button buttonFilterAll;
    private Button buttonFilterRunning;
    private Button buttonFilterIdle;
    private Button buttonFilterFinished;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }

        locationID = getIntent().getStringExtra("locationID");
        roomId   = getIntent().getStringExtra("roomId");   // ex: "bb1_room_1"
        String roomName = getIntent().getStringExtra("roomTitle"); // ex: "BB1 Room #1"
        if (roomName == null) roomName = "Laundry Room";

        Toolbar toolbar = findViewById(R.id.roomToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(roomName);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        recyclerMachines = findViewById(R.id.recyclerMachines);
        textNoReservations = findViewById(R.id.textNoReservations);

        // Initialize filter buttons
        buttonFilterAll = findViewById(R.id.buttonFilterAll);
        buttonFilterRunning = findViewById(R.id.buttonFilterRunning);
        buttonFilterIdle = findViewById(R.id.buttonFilterIdle);
        buttonFilterFinished = findViewById(R.id.buttonFilterFinished);

        // Grid of 2 columns
        recyclerMachines.setLayoutManager(new GridLayoutManager(this, 2));

        firestore = FirebaseFirestore.getInstance();

        // --- Initial mock machine list (for UI before Firebase updates) ---
        machines = new ArrayList<>();

        //Mock machine for machine details
        //machines.add(new MachineItem("Washer 3", "idle", "washer", 0));

        filteredMachines = new ArrayList<>(machines);
        machineAdapter = new MachineAdapter(filteredMachines);
        recyclerMachines.setAdapter(machineAdapter);

        textNoReservations.setText("No reservations");

        // Set up filter button click listeners
        setupFilterButtons();
        
        // Set initial filter button state
        updateFilterButtonStates();

        // --- Firestore listener to update machines in real-time ---
        listenToMachineUpdates();
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    @Override
    protected void onStart() {
        super.onStart();
        ReadDatabase();

        // Start simulated BluetoothService for each machine
//        bluetoothService = new BluetoothServices(5000L); // every 5s
//
//        for (MachineItem machine : machines) {
//            // Use machine name as document ID in Firestore
//            String machineId = machine.name.replaceAll("\\s+", "_"); // e.g., "Washer 1" -> "Washer_1"
//           bluetoothService.startReading(roomId, machineId);
//        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (bluetoothService != null) {
            bluetoothService.stopReading();
            bluetoothService = null;
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private void ReadDatabase() {
        AtomicBoolean firstReadFlag = new AtomicBoolean(true);
        CollectionReference collectionRef = firestore.collection("locations")
                .document(locationID)
                .collection("rooms")
                .document(roomId)
                .collection("machines");
        collectionRef.addSnapshotListener((snapshots, e) -> {
            machines.clear();
                for (QueryDocumentSnapshot document : snapshots) {
                    String id = document.getId();
                    String state = document.getString("state"); //Can be free, InProgress or Finished
                    String label = document.getString("label");
                    Map<String, Object> telemetry = (Map<String, Object>) document.get("telemetry");
                    String status = Objects.requireNonNull(telemetry.get("status")).toString();
                    String type = document.getString("type");
                    MachineItem machine = new MachineItem(id, label, status, type, 0);
                    machines.add(machine);
                    assert state != null;
                    if (state.equals("Finished") && !firstReadFlag.get()) {
                        displayCompletionNotification("MACHINE UPDATE", label + " is finished.");
                    }
                    if(firstReadFlag.get()){
                        firstReadFlag.set(false);
                    }

                }
            machineAdapter = new MachineAdapter(machines);
            recyclerMachines.setAdapter(machineAdapter);
        });
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    private void displayCompletionNotification(String title, String message){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            String channelId = "default_channel";
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Machine Updates",
                    NotificationManager.IMPORTANCE_HIGH);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "default_channel")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH);
        NotificationManagerCompat manager = NotificationManagerCompat.from(this);
        manager.notify((int) System.currentTimeMillis(), builder.build());
    }

    // --- Filter button setup ---
    private void setupFilterButtons() {
        buttonFilterAll.setOnClickListener(v -> applyFilter("all"));
        buttonFilterRunning.setOnClickListener(v -> applyFilter("running"));
        buttonFilterIdle.setOnClickListener(v -> applyFilter("idle"));
        buttonFilterFinished.setOnClickListener(v -> applyFilter("finished"));
    }

    // --- Apply filter to machine list ---
    private void applyFilter(String filter) {
        currentFilter = filter;
        filteredMachines.clear();

        for (MachineItem machine : machines) {
            // Normalize status: treat null/empty as "idle" (same as display logic)
            String status = machine.status != null && !machine.status.isEmpty() 
                    ? machine.status.toLowerCase() 
                    : "idle";
            boolean shouldInclude = false;

            switch (filter) {
                case "all":
                    shouldInclude = true;
                    break;
                case "running":
                    shouldInclude = status.equals("running");
                    break;
                case "idle":
                    shouldInclude = status.equals("idle");
                    break;
                case "finished":
                    shouldInclude = status.equals("finished") || status.equals("done");
                    break;
            }

            if (shouldInclude) {
                filteredMachines.add(machine);
            }
        }

        // Update button states
        updateFilterButtonStates();

        // Notify adapter of changes
        machineAdapter.notifyDataSetChanged();

        // Show/hide message based on filter and results
        if (filteredMachines.isEmpty()) {
            textNoReservations.setVisibility(TextView.VISIBLE);
            String message = "No machines found";
            switch (filter) {
                case "idle":
                    message = "No machines idle";
                    break;
                case "running":
                    message = "No machines running";
                    break;
                case "finished":
                    message = "No machines finished";
                    break;
                case "all":
                    message = "No machines";
                    break;
            }
            textNoReservations.setText(message);
        } else {
            textNoReservations.setVisibility(TextView.GONE);
        }
    }

    // --- Update filter button visual states ---
    private void updateFilterButtonStates() {
        int activeColor = 0xFF6200EE; // Purple
        int inactiveColor = 0xFF757575; // Gray

        buttonFilterAll.setBackgroundTintList(ColorStateList.valueOf(
                currentFilter.equals("all") ? activeColor : inactiveColor));
        buttonFilterRunning.setBackgroundTintList(ColorStateList.valueOf(
                currentFilter.equals("running") ? activeColor : inactiveColor));
        buttonFilterIdle.setBackgroundTintList(ColorStateList.valueOf(
                currentFilter.equals("idle") ? activeColor : inactiveColor));
        buttonFilterFinished.setBackgroundTintList(ColorStateList.valueOf(
                currentFilter.equals("finished") ? activeColor : inactiveColor));
    }

    // --- Firestore listener ---
    private void listenToMachineUpdates() {
        firestore.collection("locations")
                .document(locationID)
                .collection("rooms")
                .document(roomId)
                .collection("machines")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null) return;

                        if (value != null) {
                            for (DocumentChange dc : value.getDocumentChanges()) {
                                String id = dc.getDocument().getId();
                                String status = dc.getDocument().getString("status");
                                
                                // Only update if Firestore has an explicit status field
                                // This preserves the mock data status if Firestore doesn't provide one
                                if (status != null && !status.isEmpty()) {
                                    // Update local machine list
                                    for (int i = 0; i < machines.size(); i++) {
                                        MachineItem m = machines.get(i);
                                        String machineId = m.name.replaceAll("\\s+", "_");
                                        if (machineId.equals(id)) {
                                            // Replace old MachineItem with updated status
                                            machines.set(i, new MachineItem(m.machineID, m.name, status, m.type, m.iconResId));
                                            
                                            // Reapply current filter to update filtered list
                                            applyFilter(currentFilter);
                                            break;
                                        }
                                    }
                                }
                                // If status is null/empty, ignore this update and keep mock data status
                            }
                        }
                    }
                });
    }
}

