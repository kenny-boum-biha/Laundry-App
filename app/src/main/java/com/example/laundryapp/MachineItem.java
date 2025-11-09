package com.example.laundryapp;

public class MachineItem {
    public final String machineID;
    public final String label;      // e.g. "Washer 1"
    public final String status;    // e.g. "idle", "running", "done"
    public final String type;      // "washer" or "dryer"
    public final int iconResId;    // we'll choose based on type

    public MachineItem(String machineID, String label, String status, String type, int iconResId) {
        this.machineID = machineID;
        this.label = label;
        this.status = status;
        this.type = type;
        this.iconResId = iconResId;
    }
}
