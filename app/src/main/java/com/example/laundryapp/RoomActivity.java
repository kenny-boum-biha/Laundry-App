package com.example.laundryapp;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import android.util.Log;

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

    private String locationId;
    private String currentFilter = "all"; // "all", "running", "idle", "finished"

    // Filter buttons
    private Button buttonFilterAll;
    private Button buttonFilterRunning;
    private Button buttonFilterIdle;
    private Button buttonFilterFinished;

    private com.google.firebase.firestore.ListenerRegistration machinesListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);

        roomId   = getIntent().getStringExtra("roomId");   // ex: "bb1_room_1"
        locationId = getIntent().getStringExtra("locationID");
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
        filteredMachines = new ArrayList<>();

        //Mock machine for machine details
        //machines.add(new MachineItem("Washer 3", "idle", "washer", 0));

        filteredMachines = new ArrayList<>(machines);
        machineAdapter = new MachineAdapter(filteredMachines, locationId, roomId);
        recyclerMachines.setAdapter(machineAdapter);

        textNoReservations.setText("No reservations");

        // Set up filter button click listeners
        setupFilterButtons();
        
        // Set initial filter button state
        updateFilterButtonStates();

        // --- Firestore listener to update machines in real-time ---
        listenToMachineUpdates();
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Start simulated BluetoothService for each machine
        bluetoothService = new BluetoothServices(5000L); // every 5s

        for (MachineItem machine : machines) {
            // Use machine name as document ID in Firestore
            String machineId = machine.name.replaceAll("\\s+", "_"); // e.g., "Washer 1" -> "Washer_1"
            bluetoothService.startReading(roomId, machineId);
        }
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
        if (locationId == null || roomId == null) {
            Log.e("RoomActivity", "ERROR: LocationID or RoomID is NULL. Check MainActivity.");
            return;
        }

        machinesListener = firestore.collection("locations")
                .document(locationId)
                .collection("rooms")
                .document(roomId)
                .collection("machines")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("RoomActivity", "Firestore Listen Failed", error);
                        return;
                    }

                    if (value != null) {
                        boolean hasChanges = false;

                        for (DocumentChange dc : value.getDocumentChanges()) {
                            try {
                                // 1. Try to convert data
                                MachineItem machine = dc.getDocument().toObject(MachineItem.class);

                                // 2. Log what we found (Check Logcat!)
                                Log.d("RoomActivity", "Found Machine: " + machine.name + " (ID: " + machine.id + ")");

                                switch (dc.getType()) {
                                    case ADDED:
                                        machines.add(machine);
                                        hasChanges = true;
                                        break;

                                    case MODIFIED:
                                        for (int i = 0; i < machines.size(); i++) {
                                            // Use explicit null checks
                                            if (machines.get(i).id != null && machines.get(i).id.equals(machine.id)) {
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
                                // This prevents the app from crashing if one machine has bad data
                                Log.e("RoomActivity", "CRASH PREVENTED: Could not parse machine document: " + dc.getDocument().getId(), e);
                            }
                        }

                        if (hasChanges) {
                            applyFilter(currentFilter);
                        }
                    }
                });
    }

}

