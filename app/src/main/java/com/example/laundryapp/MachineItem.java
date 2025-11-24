package com.example.laundryapp;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.PropertyName;
import java.util.Map;

public class MachineItem {


    @DocumentId
    public String id;           // e.g. "machine_1"

    @PropertyName("label")
    public String name;         // e.g. "Washer 1"

    @PropertyName("state")
    public String status;       // e.g. "Finished"

    public String type;         // e.g. "washer"
    public String sensor;       // e.g. "ESP32-001" (THIS WAS MISSING)

    public Map<String, Object> telemetry; // Nested data

    public int iconResId;       // Internal use, likely 0 from DB

    public Boolean isReserved;  // Use nullable Boolean

    // --- CONSTRUCTORS ---

    // 1. REQUIRED: Empty constructor for Firestore
    public MachineItem() {}

    // 2. Manual constructor
    public MachineItem(String name, String status, String type, String sensor, Boolean isReserved) {
        this.name = name;
        this.status = status;
        this.type = type;
        this.sensor = sensor;
        this.isReserved = isReserved;
    }


    // Helpers
    public String getSensorId() {
        return sensor != null ? sensor : "Unknown Sensor";
    }

    //  RMS
    public String getRms() {
        if (telemetry != null && telemetry.containsKey("rms")) {
            return String.valueOf(telemetry.get("rms"));
        }
        return "0.0";
    }
    //  Activity
    public String getActivity() {
        if (telemetry != null && telemetry.containsKey("activity")) {
            return String.valueOf(telemetry.get("activity"));
        }
        return "0.0";
    }

    //  Status
    public String getStatus() {
        if (status != null && !status.isEmpty()) return status;
        if (telemetry != null && telemetry.containsKey("status")) {
            return String.valueOf(telemetry.get("status"));
        }
        return "idle";
    }

    public boolean getReservedSafe() { return isReserved != null ? isReserved : false; }
}