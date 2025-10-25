package com.example.laundryapp;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
