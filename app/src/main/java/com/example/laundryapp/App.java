package com.example.laundryapp;

import android.app.Application;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.PersistentCacheSettings;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        // Simply build PersistentCacheSettings without specifying size
        // This uses the default cache behavior
        PersistentCacheSettings cacheSettings =
                PersistentCacheSettings.newBuilder()
                        .build();

        FirebaseFirestoreSettings settings =
                new FirebaseFirestoreSettings.Builder()
                        .setLocalCacheSettings(cacheSettings)
                        .build();

        firestore.setFirestoreSettings(settings);
    }
}
