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
    private String currentFilter = "all"; // "all", "running", "idle", "finished"

    // Filter buttons
    private Button buttonFilterAll;
    private Button buttonFilterRunning;
    private Button buttonFilterIdle;
    private Button buttonFilterFinished;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);

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
        machines.add(new MachineItem("Washer 1", "idle", "washer", 0));
        machines.add(new MachineItem("Dryer 1", "idle", "dryer", 0));
        machines.add(new MachineItem("Washer 2", "running", "washer", 0));
        machines.add(new MachineItem("Dryer 2", "running", "dryer", 0));
        machines.add(new MachineItem("Washer 3", "finished", "washer", 0));
        machines.add(new MachineItem("Dryer 3", "finished", "dryer", 0));

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
            String status = machine.status != null ? machine.status.toLowerCase() : "";
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

        // Show/hide "No reservations" text
        if (filteredMachines.isEmpty()) {
            textNoReservations.setVisibility(TextView.VISIBLE);
            textNoReservations.setText("No machines found");
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
        firestore.collection("rooms")
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

                                // Update local machine list
                                for (int i = 0; i < machines.size(); i++) {
                                    MachineItem m = machines.get(i);
                                    String machineId = m.name.replaceAll("\\s+", "_");
                                    if (machineId.equals(id)) {
                                        // Replace old MachineItem with updated status
                                        machines.set(i, new MachineItem(m.name, status, m.type, m.iconResId));
                                        
                                        // Reapply current filter to update filtered list
                                        applyFilter(currentFilter);
                                        break;
                                    }
                                }
                            }
                        }
                    }
                });
    }
}

