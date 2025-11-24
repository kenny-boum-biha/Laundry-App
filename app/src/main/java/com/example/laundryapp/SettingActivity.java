package com.example.laundryapp;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.widget.Button;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.app.AppCompatDelegate;//Import to default dark mode

import com.google.firebase.firestore.FirebaseFirestore;

import android.widget.EditText;
import com.google.firebase.auth.FirebaseAuth;
import android.content.Intent;
import android.widget.TextView;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;
import java.util.ArrayList;
import android.widget.LinearLayout;
import android.view.View;

public class SettingActivity extends AppCompatActivity {
    //Variables to check if the switch are on or off
    private boolean DarkModeON;
    private boolean NotificationON;

    //Switches and button variables
    private SwitchCompat DarkModeSwitch;
    private SwitchCompat NotificationSwitch;
    //SharedPreferences
    private SharedPreferences PrefDarkMode;
    private SharedPreferences.Editor editor;
    private SharedPreferences NotificationPref;

    //Adding the location variables
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private View Location_Button;

    //Adding the email variables
    private TextView Actual_Email;
    //List of all items
    private List<SettingItems> List_Of_Items;
    //-----------------------------------------------------------------------------------------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_setting);
        //
        List_Of_Items = new ArrayList<>();
        List_Of_Items.add(new SettingItems("Change Password", "Change Password"));
        List_Of_Items.add(new SettingItems("Notification", "Notification"));
        List_Of_Items.add(new SettingItems("Dark Mode", "Dark Mode"));

        //Get firebase instance
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        //Get the email textview
        Actual_Email = findViewById(R.id.Actual_Email);
        showEmail();
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //Setting up the toolbar
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.Main_Toolbar);
        setSupportActionBar(toolbar);//Convert Toolbar into Actionbar so that it can be interactive
        if (getSupportActionBar() != null)
        {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);//Shows the back arrow to go back to the main activity
            getSupportActionBar().setTitle("Settings");//Sets the title of the activity
        }

        //Initializing the variables of the switches
        DarkModeSwitch = findViewById(R.id.switch_DarkMode);
        NotificationSwitch = findViewById(R.id.switch_notification);

        //Shared preferences to save the settings
        PrefDarkMode = getSharedPreferences("Setting_SharedPref", MODE_PRIVATE);
        editor = PrefDarkMode.edit();

        //Loads saved settings or set it as false if no data
        DarkModeON = PrefDarkMode.getBoolean("DarkModeON", false);
        NotificationON = PrefDarkMode.getBoolean("NotificationON", false);

        //Updates the UI based on the saved settings
        DarkModeSwitch.setChecked(DarkModeON);
        //Updates the Switches and apply theme
        if (DarkModeSwitch.isChecked()) {
            //Go to dark mode
            DarkModeON = true;
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }
        else {
            //Go to light(normal) mode
            DarkModeON = false;
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        //Linking variables to the switches/buttons(What actually happends when the switch is turned on/off)
        DarkModeSwitch.setOnClickListener(v ->//Dark mode button listener
        {//Updates the Switches and apply theme whenever we turn the switch on/off
            if (DarkModeSwitch.isChecked()) {
                //Go to dark mode
                DarkModeON = true;
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            }
            else {
                //Go to light(normal) mode
                DarkModeON = false;
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

            }
            saveSetting();//Calls the method to save the settings
        });

        //Button for location
        View btnLocation = findViewById(R.id.Location_Button_Container);
        btnLocation.setOnClickListener(v1 -> {
            Intent intent = new Intent(SettingActivity.this, LocationActivity.class);
            startActivity(intent);
        });

    }
    //-----------------------------------------------------------------------------------------------------------------------------
    private void saveSetting()
    {
        //Edits the sharedpref and stores the data
        editor.putBoolean("DarkModeON", DarkModeON);
        editor.putBoolean("NotificationON", NotificationON);
        editor.apply();
    }
    //-----------------------------------------------------------------------------------------------------------------------------
    @Override//Replacing the onSupportNavigateUp with this method
    public boolean onSupportNavigateUp() {
        saveSetting();
        finish();
        return true;
    }
    //-----------------------------------------------------------------------------------------------------------------------------
    private void showEmail()
    {//Show email of the user on the setting activity
        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            String email = user.getEmail();
            Actual_Email.setText(email);
        }
        else {
            Actual_Email.setText("No Email in database or not logged in");
        }

    }

    public static class SettingItems
    {
        public int Icon_ID;
        public String Title;
        public String Description;

        public SettingItems(String title, String description)
        {
            Title = title;
            Description = description;
        }
    }
}



