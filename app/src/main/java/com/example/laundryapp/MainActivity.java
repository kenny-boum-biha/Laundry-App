package com.example.laundryapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.AggregateQuery;
import com.google.firebase.firestore.AggregateSource;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;



import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private  FirebaseFirestore db;
    private RecyclerView recyclerRooms;
    private RoomAdapter roomAdapter;
    protected ImageButton signOutButton;
    private FirebaseAuth mAuth;
    private ImageButton settingButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.Main_Toolbar);
        setSupportActionBar(toolbar);

        recyclerRooms = findViewById(R.id.recyclerRooms);
        signOutButton = findViewById(R.id.signOutButton);
        recyclerRooms.setLayoutManager(new LinearLayoutManager(this));
        settingButton = findViewById(R.id.button_Setting);

        //Initializing the database
        mAuth = FirebaseAuth.getInstance();

    }

    @Override
    public void onStart() {
        super.onStart();
        this.db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            currentUser.reload();
        }
        ArrayList<RoomItem> rooms = new ArrayList<>();
        roomAdapter = new RoomAdapter(rooms, room -> {
            Intent roomActivityIntent = new Intent(MainActivity.this, RoomActivity.class);
            if (currentUser != null) {
                roomActivityIntent.putExtra("locationID", room.locationID);
                roomActivityIntent.putExtra("roomId", room.roomId);
                roomActivityIntent.putExtra("roomTitle", room.title);
                startActivity(roomActivityIntent);
            } else {
                goToLoginActivity();
            }
        });

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference collectionRef = db.collection("locations")
                .document("location_1")
                .collection("rooms");
        collectionRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                rooms.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    String roomID = document.getId();
                    String roomName = document.getString("name");
                    db.collection("locations")
                            .document("location_1")
                            .collection("rooms")
                            .document(roomID)
                            .collection("machines")
                            .get()
                            .addOnSuccessListener(query -> {
                                int machineCount = query.size();
                                rooms.add(new RoomItem("location_1",roomID, roomName, machineCount + " machines in room."));
                                roomAdapter.notifyItemInserted(rooms.size() - 1);
                            });
                }
            }
            else{
                Log.e("DEBUG", "Error reading Firestore", task.getException());
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

        //Opening the setting activity by calling the goToSettingActivity method
        settingButton.setOnClickListener(v -> goToSettingActivity());

    }

    public void goToLoginActivity(){
        Intent intent = new Intent(getApplicationContext(), loginActivity.class);
        startActivity(intent);
    }

    //Method to from Main to Settings
    public void goToSettingActivity()
    {
        //Button Listener to go from MainActivity to SettingActivity
        Intent intent = new Intent(MainActivity.this, SettingActivity.class);
        startActivity(intent);
    }
}