package com.example.laundryapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerRooms;
    private RoomAdapter roomAdapter;
    protected Button signOutButton;
    private FirebaseAuth mAuth;
    private BluetoothServiceMock mock1, mock2,mock3,mock4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        recyclerRooms = findViewById(R.id.recyclerRooms);
        signOutButton = findViewById(R.id.signOutButton);
        recyclerRooms.setLayoutManager(new LinearLayoutManager(this));

        mAuth = FirebaseAuth.getInstance();

        mock1 = new BluetoothServiceMock("location_1", "room_1", "machine_1");
        mock2 = new BluetoothServiceMock("location_1", "room_1", "machine_2");
        mock3 = new BluetoothServiceMock("location_1", "room_1", "machine_3");
        mock4 = new BluetoothServiceMock("location_1", "room_1", "machine_4");

        mock1.start();
        mock2.start();
        mock3.start();
        mock4.start();
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            currentUser.reload();
        }

        //bluetoothMock = new BluetoothServiceMock("location_1", "room_1", "machine_1");
        //bluetoothMock.start();

        //FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Simulate periodic Bluetooth readings being sent to Firestore
        /*new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                FirebaseFirestore db = FirebaseFirestore.getInstance();

                String locationId = "location_1";
                String roomId = "room_1";

                for (int i = 1; i <= 4; i++) {
                    String machineId = "machine_" + i;

                    double rms = Math.random() * 10;  // random RMS value (0–10)
                    double peak = Math.random() * 20; // random peak value (0–20)
                    String status = (rms > 5) ? "running" : "idle";

                    Map<String, Object> telemetry = new HashMap<>();
                    telemetry.put("rms", rms);
                    telemetry.put("peak", peak);

                    Map<String, Object> machineData = new HashMap<>();
                    machineData.put("label", "Machine " + i);
                    machineData.put("sensorId", "ESP32-00" + i);
                    machineData.put("status", status);
                    machineData.put("type", (i % 2 == 0) ? "dryer" : "washer");
                    machineData.put("telemetry", telemetry);

                    db.collection("locations")
                            .document(locationId)
                            .collection("rooms")
                            .document(roomId)
                            .collection("machines")
                            .document(machineId)
                            .set(machineData)
                            .addOnSuccessListener(aVoid ->
                                    Log.d("FirebaseTest", "Machine data updated"))
                            .addOnFailureListener(e ->
                                    Log.e("FirebaseTest", "Error updating Machine " + e));
                }
            }
        }, 0, 5000); // every 5 seconds

         */


        // Dummy UI setup for rooms (to navigate to RoomActivity)
        ArrayList<RoomItem> rooms = new ArrayList<>();
        rooms.add(new RoomItem("bb1_room_1", "BB1 Room #1", "4 machines available"));
        rooms.add(new RoomItem("bb1_room_2", "BB1 Room #2", "2 machines available"));

        roomAdapter = new RoomAdapter(rooms, room -> {
            Intent roomActivityIntent = new Intent(MainActivity.this, RoomActivity.class);
            if (currentUser != null) {
                roomActivityIntent.putExtra("roomId", room.roomId);
                roomActivityIntent.putExtra("roomTitle", room.title);
                startActivity(roomActivityIntent);
            } else {
                goToLoginActivity();
            }
        });

        recyclerRooms.setAdapter(roomAdapter);

        signOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAuth.signOut();
                finish();
                startActivity(getIntent());
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Stop timers when activity is destroyed
        if (mock1 != null) mock1.stop();
        if (mock2 != null) mock2.stop();
        if (mock3 != null) mock3.stop();
        if (mock4 != null) mock4.stop();
    }

    public void goToLoginActivity() {
        Intent intent = new Intent(getApplicationContext(), loginActivity.class);
        startActivity(intent);
    }
}
