package com.example.firebase_clemenisle_ev;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import com.google.firebase.auth.FirebaseAuth;

import androidx.appcompat.app.AppCompatActivity;

public class SplashScreenActivity extends AppCompatActivity {

    FirebaseAuth firebaseAuth;

    Context myContext;

    String emailAddress, password;
    boolean remember;

    private void initSharedPreferences() {
        SharedPreferences sharedPreferences = myContext
                .getSharedPreferences("login", Context.MODE_PRIVATE);
        remember = sharedPreferences.getBoolean("remember", false);
        emailAddress = sharedPreferences.getString("emailAddress", null);
        password = sharedPreferences.getString("password", null);
    }

    private void sendSharedPreferences() {
        SharedPreferences sharedPreferences = myContext
                .getSharedPreferences("login", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putBoolean("isLoggedIn", false);
        editor.putBoolean("remember", false);
        editor.apply();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        myContext = SplashScreenActivity.this;

        initSharedPreferences();

        firebaseAuth = FirebaseAuth.getInstance();
        if (!remember) {
            firebaseAuth.signOut();
            sendSharedPreferences();
        }
        proceedToNextActivity();
    }

    private void proceedToNextActivity() {
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(myContext, MainActivity.class);
            startActivity(intent);
            finish();
        }, 3000);
    }
}