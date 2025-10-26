package com.example.laundryapp;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;
import android.util.Log;
import java.util.Timer;
import java.util.TimerTask;



import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerRooms;
    private RoomAdapter roomAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        recyclerRooms = findViewById(R.id.recyclerRooms);
        recyclerRooms.setLayoutManager(new LinearLayoutManager(this));

        // Firestore test
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        /*Map<String, Object> testData = new HashMap<>();
        testData.put("message", "Hello Firebase!");
        testData.put("timestamp", System.currentTimeMillis()); */

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                Map<String, Object> testData = new HashMap<>();
                testData.put("message", "Hello Firebase!");
                testData.put("timestamp", System.currentTimeMillis());
                db.collection("test").document("ping").set(testData);
                db.collection("test")
                        .document("ping")
                        .set(testData)
                        .addOnSuccessListener(aVoid -> Log.d("FirebaseTest", "Test document written successfully"))
                        .addOnFailureListener(e -> Log.e("FirebaseTest", "Failed to write test document", e));


            }
        }, 0, 5000); // every 5 seconds

        // TODO: later replace this with Firestore query of "rooms" collection
        ArrayList<RoomItem> rooms = new ArrayList<>();
        rooms.add(new RoomItem("bb1_room_1", "BB1 Room #1", "4 machines available"));
        rooms.add(new RoomItem("bb1_room_2", "BB1 Room #2", "2 machines available"));

        roomAdapter = new RoomAdapter(rooms, room -> {
            Intent i = new Intent(MainActivity.this, RoomActivity.class);
            i.putExtra("roomId", room.roomId);
            i.putExtra("roomTitle", room.title);
            startActivity(i);
        });

        recyclerRooms.setAdapter(roomAdapter);
    }

}
