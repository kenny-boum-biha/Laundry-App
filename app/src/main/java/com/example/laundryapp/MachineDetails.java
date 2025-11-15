package com.example.laundryapp;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.widget.TextView;

public class MachineDetails extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_machine_details);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        TextView MachineNameTV = findViewById(R.id.Machine_Name_ID);//TV=TextView
        TextView CycleTimeTV = findViewById(R.id.Cycle_Time_ID);
        TextView TelemetryTV = findViewById(R.id.Telemetry_ID);

        //Get data
        String machineName = getIntent().getStringExtra("machineName");
        String cycleTime = getIntent().getStringExtra("cycleTime");
        String telemetry = getIntent().getStringExtra("telemetry");

        //
        MachineNameTV.setText(machineName);
        CycleTimeTV.setText("Cycle time : "+ cycleTime);
        TelemetryTV.setText(telemetry);

    }
}