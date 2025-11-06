
package com.example.laundryapp;

import android.util.Log;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Random;

public class BluetoothServiceMock {
    private static final String TAG = "BluetoothServiceMock";
    private final String locationId;
    private final String roomId;
    private final String machineId;
    private final FirebaseFirestore db;
    private Timer timer;
    private final Random random = new Random();

    public BluetoothServiceMock(String locationId, String roomId, String machineId) {
        this.locationId = locationId;
        this.roomId = roomId;
        this.machineId = machineId;
        this.db = FirebaseFirestore.getInstance();
    }

    public void start() {
        Log.d(TAG, "Timer started for " + machineId);
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Random random = new Random();
                double rms = (10 * random.nextDouble());
                double peak = rms * (1.5 + random.nextDouble());
                long timestamp = System.currentTimeMillis();

                int machineNumber = 1;
                try {
                    machineNumber = Integer.parseInt(machineId.replace("machine_", ""));
                } catch (Exception e) {
                    Log.e(TAG, "Invalid machine ID format: " + machineId);
                }

                // 🧺 Determine label and type based on machine number
                String label;
                String type;
                if (machineNumber % 2 == 1) { // odd = washer
                    label = "Washer " + ((machineNumber + 1) / 2);
                    type = "washer";
                } else { // even = dryer
                    label = "Dryer " + (machineNumber / 2);
                    type = "dryer";
                }

                Map<String, Object> telemetry = new HashMap<>();
                telemetry.put("rms", rms);
                telemetry.put("peak", peak);
                telemetry.put("timestamp", timestamp);

                Map<String, Object> machineData = new HashMap<>();
                machineData.put("label", label);
                machineData.put("sensorId", "ESP32-" + machineId);
                machineData.put("status", rms > 4 ? "running" : "idle");
                machineData.put("telemetry", telemetry);
                machineData.put("type", type);

                db.collection("locations")
                        .document(locationId)
                        .collection("rooms")
                        .document(roomId)
                        .collection("machines")
                        .document(machineId)
                        .set(machineData)
                        .addOnSuccessListener(aVoid ->
                                Log.d("BluetoothServiceMock", "Updated " + machineId + " successfully"))
                        .addOnFailureListener(e ->
                                Log.e("BluetoothServiceMock", "Failed to update " + machineId, e));
            }
        }, 0, 2000); // every 5 seconds
    }

    public void stop() {
        if (timer != null) {
            timer.cancel();
        }
    }
}

