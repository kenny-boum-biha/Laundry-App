package com.example.laundryapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

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



import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerRooms;
    private RoomAdapter roomAdapter;
    protected Button signOutButton;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        recyclerRooms = findViewById(R.id.recyclerRooms);
        signOutButton = findViewById(R.id.signOutButton);
        recyclerRooms.setLayoutManager(new LinearLayoutManager(this));
        //Initializing the database
        mAuth = FirebaseAuth.getInstance();

    }
    @Override
    public void onStart() {
        super.onStart();
        //Check if user is logged in, if so, update server side data
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            currentUser.reload();
        }

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
            Intent roomActivityIntent = new Intent(MainActivity.this, RoomActivity.class);
            if(currentUser != null){
                roomActivityIntent.putExtra("roomId", room.roomId);
                roomActivityIntent.putExtra("roomTitle", room.title);
                startActivity(roomActivityIntent);
            }
            else{
                goToLoginActivity();
            }

        recyclerRooms.setAdapter(roomAdapter);
        signOutButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                mAuth.signOut();
                finish();
                startActivity(getIntent());
            }
        });
    }
    public void goToLoginActivity(){
        Intent intent = new Intent(getApplicationContext(), loginActivity.class);
        startActivity(intent);
    }
}
