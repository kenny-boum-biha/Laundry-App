package com.example.laundryapp;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class RoomActivity extends AppCompatActivity {

    private RecyclerView recyclerMachines;
    private TextView textNoReservations;
    private MachineAdapter machineAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);

        String roomId   = getIntent().getStringExtra("roomId");   // ex: "bb1_room_1"
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

        // Grid of 2 columns, like screenshot
        GridLayoutManager glm = new GridLayoutManager(this, 2);
        recyclerMachines.setLayoutManager(glm);

        // TODO: in final version, query Firestore:
        // firestore.collection("rooms").document(roomId).collection("machines").get() ...
        // For now: mock list
        ArrayList<MachineItem> machines = new ArrayList<>();
        machines.add(new MachineItem("Washer 1", "idle", "washer", 0));
        machines.add(new MachineItem("Dryer 1", "running", "dryer", 0));
        machines.add(new MachineItem("Washer 2", "running", "washer", 0));
        machines.add(new MachineItem("Dryer 2", "idle", "dryer", 0));

        machineAdapter = new MachineAdapter(machines);
        recyclerMachines.setAdapter(machineAdapter);

        textNoReservations.setText("No reservations");
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
