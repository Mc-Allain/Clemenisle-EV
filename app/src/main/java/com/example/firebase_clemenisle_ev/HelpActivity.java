package com.example.firebase_clemenisle_ev;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.firebase_clemenisle_ev.Classes.FirebaseURL;
import com.example.firebase_clemenisle_ev.Classes.HelpEntry;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

public class HelpActivity extends AppCompatActivity {

    private final static String firebaseURL = FirebaseURL.getFirebaseURL();
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance(firebaseURL);
    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;

    ConstraintLayout helpEntryInputLayout, loginHelpLayout, userEntryLayout;
    TextView tvUserEntry2;
    EditText etHelpEntry;
    ImageView editImage;
    Button submitButton, loginButton;
    ProgressBar progressBar;

    Context myContext;
    Resources myResources;

    String userId;

    boolean isLoggedIn = false;

    String helpEntry, helpEntryValue;

    DatabaseReference usersRef;

    private void initSharedPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences("login", Context.MODE_PRIVATE);
        isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);
    }

    private void sendLoginPreferences() {
        SharedPreferences sharedPreferences = myContext.getSharedPreferences(
                "login", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putBoolean("isLoggedIn", false);
        editor.putBoolean("remember", false);
        editor.putString("emailAddress", null);
        editor.putString("password", null);
        editor.apply();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        helpEntryInputLayout = findViewById(R.id.helpEntryInputLayout);
        loginHelpLayout = findViewById(R.id.loginHelpLayout);
        userEntryLayout = findViewById(R.id.userEntryLayout);
        tvUserEntry2 = findViewById(R.id.tvUserEntry2);
        etHelpEntry = findViewById(R.id.etHelpEntry);
        editImage = findViewById(R.id.editImage);
        submitButton = findViewById(R.id.submitButton);
        loginButton = findViewById(R.id.loginButton);
        progressBar = findViewById(R.id.progressBar);

        myContext = HelpActivity.this;
        myResources = myContext.getResources();

        initSharedPreferences();

        firebaseAuth = FirebaseAuth.getInstance();
        if(isLoggedIn) {
            firebaseUser = firebaseAuth.getCurrentUser();
            if(firebaseUser != null) firebaseUser.reload();
            if(firebaseUser == null) {
                firebaseAuth.signOut();
                sendLoginPreferences();

                Toast.makeText(
                        myContext,
                        "Failed to get the current user",
                        Toast.LENGTH_LONG
                ).show();
            }
            else {
                userId = firebaseUser.getUid();
            }
        }

        if(userId != null) {
            loginHelpLayout.setVisibility(View.GONE);
            helpEntryInputLayout.setVisibility(View.VISIBLE);
            userEntryLayout.setVisibility(View.VISIBLE);

            usersRef = firebaseDatabase.getReference("users").child(userId);
            getSubmittedQuestion();
        }
        else {
            loginHelpLayout.setVisibility(View.VISIBLE);
            helpEntryInputLayout.setVisibility(View.GONE);
            userEntryLayout.setVisibility(View.GONE);
        }

        etHelpEntry.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                helpEntry = etHelpEntry.getText().toString();

                if(helpEntry.length() > 0) submitButton.setEnabled(true);
                else submitButton.setEnabled(false);
            }
        });

        submitButton.setOnClickListener(view -> submitQuestion());

        loginButton.setOnClickListener(view -> {
            Intent newIntent = new Intent(myContext, LoginActivity.class);
            startActivity(newIntent);
        });

        editImage.setOnClickListener(view -> {
            helpEntryInputLayout.setVisibility(View.VISIBLE);
            userEntryLayout.setVisibility(View.GONE);

            etHelpEntry.setText(helpEntryValue);

            if(helpEntry.length() > 0) submitButton.setEnabled(true);
            else submitButton.setEnabled(false);
        });
    }

    private void setOnScreenEnabled(boolean value) {
        helpEntryInputLayout.setEnabled(value);
        submitButton.setEnabled(value);
    }

    private void getSubmittedQuestion() {
        progressBar.setVisibility(View.VISIBLE);
        setOnScreenEnabled(false);

        usersRef.child("helpEntry").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    helpEntryInputLayout.setVisibility(View.GONE);
                    userEntryLayout.setVisibility(View.VISIBLE);

                    helpEntryValue = snapshot.child("value").getValue(String.class);
                    tvUserEntry2.setText(helpEntryValue);
                }
                else {
                    helpEntryInputLayout.setVisibility(View.VISIBLE);
                    userEntryLayout.setVisibility(View.GONE);
                }

                progressBar.setVisibility(View.GONE);
                setOnScreenEnabled(true);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(
                        myContext,
                        error.toString(),
                        Toast.LENGTH_LONG
                ).show();

                progressBar.setVisibility(View.GONE);
                setOnScreenEnabled(true);
            }
        });
    }

    private void submitQuestion() {
        progressBar.setVisibility(View.VISIBLE);

        usersRef.child("helpEntry").setValue(new HelpEntry(helpEntry)).
                addOnCompleteListener(task -> {
            if(!task.isSuccessful()) {
                if(task.getException() != null) {

                    String error = task.getException().toString();
                    Toast.makeText(
                            myContext,
                            error,
                            Toast.LENGTH_LONG
                    ).show();
                }
            }
            progressBar.setVisibility(View.GONE);
        });

        helpEntryInputLayout.setVisibility(View.GONE);
        userEntryLayout.setVisibility(View.VISIBLE);
    }
}