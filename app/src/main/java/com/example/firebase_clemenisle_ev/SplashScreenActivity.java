package com.example.firebase_clemenisle_ev;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import com.example.firebase_clemenisle_ev.Classes.AppMetaData;
import com.example.firebase_clemenisle_ev.Classes.FirebaseURL;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class SplashScreenActivity extends AppCompatActivity {

    private final static String firebaseURL = FirebaseURL.getFirebaseURL();
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance(firebaseURL);

    FirebaseAuth firebaseAuth;

    Context myContext;

    String emailAddress, password;
    boolean remember, inDriverModule;

    AppMetaData appMetaData;

    List<String> statusPromptArray = Arrays.asList("Under Development", "Under Maintenance");

    private void initSharedPreferences() {
        SharedPreferences sharedPreferences = myContext
                .getSharedPreferences("login", Context.MODE_PRIVATE);
        remember = sharedPreferences.getBoolean("remember", false);
        emailAddress = sharedPreferences.getString("emailAddress", null);
        password = sharedPreferences.getString("password", null);
        inDriverModule = sharedPreferences.getBoolean("inDriverModule", false);
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

        appMetaData = new AppMetaData();
        getAppMetaData();
    }

    private void getAppMetaData() {
        DatabaseReference metaDataRef = firebaseDatabase.getReference("appMetaData");
        metaDataRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String aboutApp = "Failed to get data";
                double latestVersion = 0;
                String status = "Failed to get data";

                if(snapshot.exists()) {
                    if(snapshot.child("about").exists())
                        aboutApp = snapshot.child("about").getValue(String.class);
                    if(snapshot.child("version").exists())
                        latestVersion = snapshot.child("version").getValue(Double.class);
                    if(snapshot.child("status").exists())
                        status = snapshot.child("status").getValue(String.class);
                }

                appMetaData.setAboutApp(aboutApp);
                appMetaData.setLatestVersion(latestVersion);
                appMetaData.setStatus(status);

                Intent intent;
                if(statusPromptArray.contains(status) && !appMetaData.isDeveloper()) {
                    intent = new Intent(myContext, AppStatusActivity.class);
                    intent.putExtra("isErrorStatus", false);
                }
                else if(inDriverModule) intent = new Intent(myContext, DriverActivity.class);
                else intent = new Intent(myContext, MainActivity.class);

                startActivity(intent);
                finishAffinity();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(
                        myContext,
                        error.toString(),
                        Toast.LENGTH_LONG
                ).show();

                if(!appMetaData.isDeveloper()) {
                    Intent intent = new Intent(myContext, AppStatusActivity.class);
                    intent.putExtra("isErrorStatus", true);
                    startActivity(intent);
                    finishAffinity();
                }
            }
        });
    }
}