package com.example.laundryapp;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.widget.Button;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatDelegate;//Import to default dark mode

import com.google.firebase.firestore.FirebaseFirestore;

import android.widget.EditText;
import com.google.firebase.auth.FirebaseAuth;
import android.content.Intent;
import android.widget.TextView;
import com.google.firebase.auth.FirebaseUser;

public class SettingActivity extends AppCompatActivity {
    //Variables to check if the switch are on or off
    private boolean DarkModeON;
    private boolean NotificationON;

    //Switches and button variables
    private Switch DarkModeSwitch;
    private Switch NotificationSwitch;
    private Button LanguageButton;
    //SharedPreferences
    private SharedPreferences PrefDarkMode;
    private SharedPreferences.Editor editor;

    //Adding the location variables
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private EditText editTextPW;
    private Button Location_Button;

    //Adding the email variables
    private TextView Actual_Email;
    //-----------------------------------------------------------------------------------------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_setting);

        //Get firebase instance
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        Location_Button = findViewById(R.id.Location_Button);

        //Get the email textview
        Actual_Email = findViewById(R.id.Actual_Email);
        showEmail();
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //Setting up the toolbar
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);//Convert Toolbar into Actionbar so that it can be interactive
        if (getSupportActionBar() != null)
        {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);//Shows the back arrow to go back to the main activity
            getSupportActionBar().setTitle("Setting");//Sets the title of the activity
        }

        //Initializing the variables of the switches
        DarkModeSwitch = findViewById(R.id.switch_DarkMode);
        NotificationSwitch = findViewById(R.id.switch_notification);
        LanguageButton = findViewById(R.id.button_language);

        //Shared preferences to save the settings
        PrefDarkMode = getSharedPreferences("Setting_SharedPref", MODE_PRIVATE);
        editor = PrefDarkMode.edit();

        //Loads saved settings or set it as false if no data
        DarkModeON = PrefDarkMode.getBoolean("DarkModeON", false);

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
        Location_Button = findViewById(R.id.Location_Button);
        Location_Button.setOnClickListener(v1 -> {
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

}

