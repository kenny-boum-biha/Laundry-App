package com.example.laundryapp;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recycler;
    private MachineAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Title in the toolbar collapses; we keep custom hero texts instead.
        if (getSupportActionBar() != null) getSupportActionBar().setTitle("");

        // (Optional) set hero subtitles dynamically
        TextView title = findViewById(R.id.titleHero);
        TextView sub   = findViewById(R.id.subtitleHero);
        title.setText("LAUNDRY PRO");

        recycler = findViewById(R.id.recycler);
        recycler.setLayoutManager(new LinearLayoutManager(this));

        // Demo data — replace with your live data later
        ArrayList<Machine> data = new ArrayList<>();
        data.add(new Machine("WASH", "1 available", R.drawable.ic_wash)); // you can use your own icons
        data.add(new Machine("DRY", "1 available", R.drawable.ic_dry));
        data.add(new Machine("BB1 Room #1", "4 machines", 0));
        data.add(new Machine("BB1 Room #2", "2 machines", 0));

        adapter = new MachineAdapter(data, item -> {
            // TODO: handle click (open room, open reservation, etc.)
        });
        recycler.setAdapter(adapter);
    }
}
