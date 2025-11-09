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

    //-----------------------------------------------------------------------------------------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_setting);
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
}

