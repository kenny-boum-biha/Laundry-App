package com.example.laundryapp;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.widget.ListView;
import java.util.List;
import java.util.Arrays;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;
import android.widget.TextView;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class LocationActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    //private TextView Current_Location_TV;
    private String Current_Location_String = "location_1";//To be changed to a default location
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_location);

        //Getting the database instance
        db = FirebaseFirestore.getInstance();

        ListView listView = findViewById(R.id.listView);
        //Current_Location_TV = findViewById(R.id.Current_Location);

        //Showing the lisview objects
        List<String> locations = Arrays.asList("Current Location ", "Change Location ", "Add Location ", "Delete Location ");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, locations);

        listView.setAdapter(adapter);

        //Switch case to deal with all the listview objects
        listView.setOnItemClickListener((parent, view, position, id) ->
        {
            switch(position)
            {
                case 0:
                    //Current Location
                    currentLocation();
                    break;
                case 1:
                    //Edit Location
                    changeLocation();
                    break;
                case 2:
                    //Add Location
                    addLocation();
                    break;
                case 3:
                    //Delete Location
                    deleteLocation();
                    break;
           }
        });


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

    }

    //--------------------------------------------------------------------------------------------------------------------------------------
    private void currentLocation()
    {
        //getting the collection from firestore
        db.collection("locations")
                .document(Current_Location_String)
                .get()
                .addOnSuccessListener(documentSnapshot ->
                {
                    if (documentSnapshot.exists())
                    {
                        String Location_Name = documentSnapshot.getString("name");
                        String Location_Address = documentSnapshot.getString("address");

                        String Current_Location = "Current Location: " + Location_Name + "\nCurrent Address: " + Location_Address;
                        Toast.makeText(LocationActivity.this, Current_Location, Toast.LENGTH_LONG).show();
                    }
                });
    }
    //--------------------------------------------------------------------------------------------------------------------------------------
    private void addLocation()//to be added
    {
        Toast.makeText(LocationActivity.this, "Add Location", Toast.LENGTH_SHORT).show();
    }
    //--------------------------------------------------------------------------------------------------------------------------------------
    private void deleteLocation()//to be added
    {
        Toast.makeText(LocationActivity.this, "Delete Location", Toast.LENGTH_SHORT).show();
    }
    //--------------------------------------------------------------------------------------------------------------------------------------
    private void changeLocation()
    {
        db.collection("locations")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots ->
                {
                    String[] documentSnapshots = new String[queryDocumentSnapshots.size()];
                    int i = 0;

                    //Looping through the whole collection to get all id's
                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        documentSnapshots[i++] = documentSnapshot.getId();
                    }

                    //Pop-up allowing user to change location
                    androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
                    builder.setTitle("Choose new location");
                    builder.setItems(documentSnapshots, (dialog, which) -> {
                        String Choose_Doc = documentSnapshots[which];
                        //Updates the UI
                        Current_Location_String = Choose_Doc;
                        db.collection("locations")
                                .document(Choose_Doc)
                                .get()
                                .addOnSuccessListener(documentSnapshot ->
                                {
                                    if (documentSnapshot.exists()) {
                                        String Location_Name = documentSnapshot.getString("name");
                                        String Location_Address = documentSnapshot.getString("address");

                                        String Current_Location = "Current Location: " + Location_Name + "\nCurrent Address: " + Location_Address;
                                        Toast.makeText(LocationActivity.this, "Location changed to " + Location_Name, Toast.LENGTH_LONG).show();
                                        //Current_Location_TV.setText(Current_Location);
                                    }
                                });
                    });
                    builder.show();
                });

    }
}
