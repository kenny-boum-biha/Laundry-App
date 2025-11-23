package com.example.laundryapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

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
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            currentUser.reload();
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // TODO: later replace this with Firestore query of "rooms" collection
        ArrayList<RoomItem> rooms = new ArrayList<>();
        rooms.add(new RoomItem("location_1","room_2", "Concordia Basement", "4 machines available"));

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