package com.example.laundryapp;

import android.os.Bundle;
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
    private ArrayList<MachineItem> machines;
    private BluetoothServices bluetoothService;
    private FirebaseFirestore firestore;

    private String roomId;
    private String locationID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);

        locationID  = getIntent().getStringExtra("locationID");
        roomId  = getIntent().getStringExtra("roomId");   // ex: "bb1_room_1"
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

        // Grid of 2 columns
        recyclerMachines.setLayoutManager(new GridLayoutManager(this, 2));

        firestore = FirebaseFirestore.getInstance();

        // --- Initial mock machine list (for UI before Firebase updates) ---
        machines = new ArrayList<>();
        machines.add(new MachineItem("machine_1","Washer 1", "idle", "washer", 0));
        machines.add(new MachineItem("machine_2","Dryer 1", "idle", "dryer", 0));
        machines.add(new MachineItem("machine_3","Washer 2", "idle", "washer", 0));
        machines.add(new MachineItem("machine_4","Dryer 2", "idle", "dryer", 0));

        machineAdapter = new MachineAdapter(machines);
        recyclerMachines.setAdapter(machineAdapter);

        textNoReservations.setText("No reservations");

        // --- Firestore listener to update machines in real-time ---
        listenToMachineUpdates();
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Start simulated BluetoothService for each machine

        for (MachineItem machine : machines) {
            bluetoothService = new BluetoothServices(5000L); // every 5s
            // Use machine name as document ID in Firestore
            String machineId = machine.machineID.replaceAll("\\s+", "_"); // e.g., "Washer 1" -> "Washer_1"
            bluetoothService.startReading(locationID, roomId, machineId, machine.label);
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

                                // Update local machine list
                                for (MachineItem m : machines) {
                                    String machineId = m.machineID.replaceAll("\\s+", "_");
                                    if (machineId.equals(id)) {
                                        // Replace old MachineItem with updated status
                                        machines.set(machines.indexOf(m),
                                                new MachineItem(m.machineID, m.label, status, m.type, m.iconResId));
                                        machineAdapter.notifyItemChanged(machines.indexOf(m));
                                        break;
                                    }
                                }
                            }
                        }
                    }
                });
    }
}

