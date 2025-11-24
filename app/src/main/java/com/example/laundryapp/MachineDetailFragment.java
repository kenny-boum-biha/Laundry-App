package com.example.laundryapp;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton; // Changed to ImageButton
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.FirebaseFirestoreException;

public class MachineDetailFragment extends DialogFragment {

    private static final String ARG_KEY_PATH = "selected_machine_path";
    private String documentPath;
    private FirebaseFirestore db;
    private ListenerRegistration registration;

    private TextView tvName, tvState, tvTelemetry;

    private View btnReserve;

    public static MachineDetailFragment newInstance(String fullPath) {
        MachineDetailFragment fragment = new MachineDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_KEY_PATH, fullPath);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_machine_detail, container, false);

        if (getArguments() != null) {
            documentPath = getArguments().getString(ARG_KEY_PATH);
        }

        // 1. BIND VIEWS (Make sure these IDs exist in fragment_machine_detail.xml!)
        tvName = view.findViewById(R.id.popup_machine_name);
        tvState = view.findViewById(R.id.popup_cycle_time);
        tvTelemetry = view.findViewById(R.id.popup_telemetry);
        btnReserve = view.findViewById(R.id.btn_reserve);

        if (btnReserve != null) {
            btnReserve.setOnClickListener(v -> reserveMachine());
        }

        ImageButton btnClose = view.findViewById(R.id.btn_close);
        if (btnClose != null) {
            btnClose.setOnClickListener(v -> dismiss());
        }

        db = FirebaseFirestore.getInstance();

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        // Resize dialog
        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        if (documentPath != null) {
            DocumentReference docRef = db.document(documentPath);

            // 2. START LISTENING
            registration = docRef.addSnapshotListener((snapshot, e) -> {
                if (e != null) {
                    Log.e("MachineDetail", "Listen failed.", e);
                    return;
                }

                if (snapshot != null && snapshot.exists()) {
                    // 3. SAFE CONVERSION
                    try {
                        MachineItem m = snapshot.toObject(MachineItem.class);

                        if (m != null) {
                            // 4. UPDATE UI SAFELY (Check for null views)
                            if (tvName != null) tvName.setText(m.name);
                            if (tvState != null) tvState.setText(m.getStatus());
                            // Reservation for the UI
                            if (btnReserve != null) {
                                if (m.isReserved) {
                                    btnReserve.setEnabled(false);
                                    btnReserve.setAlpha(0.5f);
                                    ((TextView) btnReserve).setText("Reserved");
                                } else {
                                    btnReserve.setEnabled(true);
                                    btnReserve.setAlpha(1f);
                                    ((TextView) btnReserve).setText("Reserve");
                                }
                            }

                            String details =
                                    "Sensor: " + m.getSensorId() + "\n" +
                                            "Type:   " + m.type + "\n\n" +
                                            "--- Telemetry ---\n" +
                                            "RMS:      " + m.getRms() + "\n" +
                                            "Activity: " + m.getActivity();

                            if (tvTelemetry != null) tvTelemetry.setText(details);
                        }
                    } catch (Exception ex) {
                        Log.e("MachineDetail", "Crash prevented during UI update", ex);
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "Error displaying data", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });
        }
    }
    private void reserveMachine() {
        DocumentReference docRef = db.document(documentPath);

        db.runTransaction(transaction -> {
            var snap = transaction.get(docRef);

            Boolean isReserved = snap.getBoolean("isReserved");
            if (isReserved != null && isReserved) {
                throw new FirebaseFirestoreException(
                        "Already reserved",
                        FirebaseFirestoreException.Code.ABORTED
                );
            }

            transaction.update(docRef, "isReserved", true);
            return null;
        }).addOnSuccessListener(unused -> {
            if (btnReserve != null) {
                btnReserve.setEnabled(false);
                btnReserve.setAlpha(0.5f);
                ((TextView) btnReserve).setText("Reserved");
            }
        }).addOnFailureListener(e -> {
            if (btnReserve != null) {
                btnReserve.setEnabled(false);
                btnReserve.setAlpha(0.5f);
                ((TextView) btnReserve).setText("Reserved by someone else");
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        if (registration != null) {
            registration.remove();
        }
    }
}