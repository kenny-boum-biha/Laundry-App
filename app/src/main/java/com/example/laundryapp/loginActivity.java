package com.example.laundryapp;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.concurrent.TimeUnit;

import android.content.SharedPreferences;
import android.content.DialogInterface;

import java.util.Map;
import java.util.HashMap;
import com.google.firebase.firestore.FieldValue;

public class loginActivity extends AppCompatActivity {
    protected EditText editTextPW;
    protected EditText editTextEmail;
    protected Button loginButton;
    protected Button createAccButton;
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.loginActivity), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        //Get firebase instance
        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        editTextPW = findViewById(R.id.editTextPassword);
        editTextEmail = findViewById(R.id.editTextEmail);
        loginButton = findViewById(R.id.signInButton);
        createAccButton = findViewById(R.id.createAccountButton);
        Toolbar loginToolbar = findViewById(R.id.loginToolbar);
        setSupportActionBar(loginToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Create or Login");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        loginButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                signIn(editTextEmail.getText().toString(), editTextPW.getText().toString());
            }
        });
        createAccButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                createAccount(editTextEmail.getText().toString(), editTextPW.getText().toString());
            }
        });
    }
    private void createAccount(String email, String password) {
        // [START create_user_with_email]
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();

                            Toast.makeText(loginActivity.this, "Authentication Success.",
                                    Toast.LENGTH_SHORT).show();

                            consentForm();//Pop-up the consent form

                            try {
                                TimeUnit.SECONDS.sleep(2);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }

                            //updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(loginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            //updateUI(null);
                        }
                    }
                });
    }
    private void signIn(String email, String password) {
        // [START sign_in_with_email]
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                            Toast.makeText(loginActivity.this, "Login Success.",
                                    Toast.LENGTH_SHORT).show();

                            consentForm();//Pop-up the consent form

                            try {
                                TimeUnit.SECONDS.sleep(2);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }

                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(loginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        // [END sign_in_with_email]
    }

    //Consent Popup
    private void consentForm() {
        SharedPreferences prefs = getSharedPreferences("consent", MODE_PRIVATE);
        boolean consent = prefs.getBoolean("consent", false);

        if (consent == false) {//Pops up a dialog box that holds the consent form if the user has not already given consent
            android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
            builder.setTitle("Privacy Policy");
            builder.setMessage("Please read the privacy policy before continuing\n" + "By using our application you are consenting to our privacy policy which includes the collection and use of the personal information you provide to us.");
            builder.setPositiveButton("Agree", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i)
                {
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putBoolean("consent", true);//Set the user as accepting the consent form
                    editor.apply();

                    finish();
                }
            });

            builder.setCancelable(false);
            builder.show();
        }
        else
        {
            finish();
        }
    }

}
