package com.example.laundryapp;

import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * BluetoothService simulates reading vibration data from a Bluetooth sensor
 * and updates the Firestore database with the machine status.
 *
 * Later, replace the simulated data with real Bluetooth input.
 */
public class BluetoothServices {

    private static final String TAG = "BluetoothService";
    private final FirebaseFirestore db;
    private final ScheduledExecutorService executor;
    private final Random random;
    private boolean running = false;
    private final long intervalMs;

    public BluetoothServices(long intervalMs) {
        this.db = FirebaseFirestore.getInstance();
     this.executor = Executors.newSingleThreadScheduledExecutor();
     this.random = new Random();
     this.intervalMs = intervalMs; // e.g. 5000ms (5 seconds)
     }
      /*
     * Starts periodically simulating vibration readings for a machine.
     *
     * @param roomId     Firestore document ID for the room
     * @param machineId  Firestore document ID for the machine
     */
    public void startReading(String roomId, String machineId) {
        if (running) return;
        running = true;

        Log.d(TAG, "Starting simulated Bluetooth vibration readings...");

        // Use schedule() instead of scheduleAtFixedRate() to comply with Android guidance
        executor.schedule(new Runnable() {
            @Override
            public void run() {
                if (!running) return;

                // --- Simulated vibration data ---
                double vibration = 0.5 + random.nextDouble() * 3.5; // between 0.5 and 4.0
                boolean isRunning = vibration > 2.0; // arbitrary threshold

                // --- Update Firestore ---
                Map<String, Object> data = new HashMap<>();
                data.put("vibration", vibration);
                data.put("isRunning", isRunning);
                data.put("timestamp", System.currentTimeMillis());

                db.collection("rooms")
                        .document(roomId)
                        .collection("machines")
                        .document(machineId)
                        .set(data)
                        .addOnSuccessListener(aVoid -> Log.d(TAG, "Data updated: " + data))
                        .addOnFailureListener(e -> Log.e(TAG, "Error updating Firestore", e));

                // Reschedule next run
                if (running) {
                    executor.schedule(this, intervalMs, TimeUnit.MILLISECONDS);
                }
            }
        }, 0, TimeUnit.MILLISECONDS);
    }

    /**
     * Stops the background vibration simulation.
     */
    public void stopReading() {
        running = false;
        executor.shutdownNow();
        Log.d(TAG, "BluetoothService stopped.");
    }
}
