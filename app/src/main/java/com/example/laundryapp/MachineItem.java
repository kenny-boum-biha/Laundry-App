package com.example.laundryapp;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.PropertyName;
import java.util.Map;

public class MachineItem {
    public final String machineID;
    public final String name;      // e.g. "Washer 1"
    public final String status;    // e.g. "idle", "running", "done"
    public final String type;      // "washer" or "dryer"
    public final int iconResId;    // we'll choose based on type
    //Data for machine details
    public String cycleTime;
    public String telemetry;


    public MachineItem(String machineID, String name, String status, String type, int iconResId) {
        this.machineID = machineID;
        this.name = name;
        this.status = status;
        this.type = type;
        this.sensor = sensor;
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
}