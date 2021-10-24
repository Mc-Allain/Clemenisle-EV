package com.example.firebase_clemenisle_ev.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.firebase_clemenisle_ev.Adapters.SettingsAdapter;
import com.example.firebase_clemenisle_ev.Classes.FirebaseURL;
import com.example.firebase_clemenisle_ev.Classes.Setting;
import com.example.firebase_clemenisle_ev.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class SettingsFragment extends Fragment {

    private final static String firebaseURL = FirebaseURL.getFirebaseURL();
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance(firebaseURL);
    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;

    RecyclerView settingsView;
    ProgressBar progressBar;

    Context myContext;

    SettingsAdapter settingsAdapter;
    List<Setting> settingList = Arrays.asList(
            new Setting(R.drawable.ic_baseline_info_24, "About"),
            new Setting(R.drawable.ic_baseline_settings_24, "Preferences"),
            new Setting(R.drawable.ic_baseline_help_24, "Help"),
            new Setting(R.drawable.ic_baseline_electric_rickshaw_24, "Driver Mode"),
            new Setting(R.drawable.ic_baseline_logout_24, "Log out")
    );
    List<Setting> settings = new ArrayList<>();

    String userId;
    boolean isLoggedIn = false, isDriver = false;

    private void initSharedPreferences() {
        SharedPreferences sharedPreferences = myContext
                .getSharedPreferences("login", Context.MODE_PRIVATE);
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        settingsView = view.findViewById(R.id.settingsView);
        progressBar = view.findViewById(R.id.progressBar);

        myContext = getContext();

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

        LinearLayoutManager linearLayout =
                new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        settingsView.setLayoutManager(linearLayout);

        settingsAdapter = new SettingsAdapter(myContext, settings);
        settingsView.setAdapter(settingsAdapter);

        if(userId == null) setSettings();
        else checkIfDriver();

        return view;
    }

    private void setSettings() {
        settings.clear();

        for(Setting setting : settingList) {
            if(setting.getSettingName().equals("Log out")) {
                if(isLoggedIn) settings.add(setting);
            }
            else if(setting.getSettingName().equals("Driver Mode")) {
                if(isDriver) settings.add(setting);
            }
            else {
                settings.add(setting);
            }
        }

        settingsAdapter.notifyDataSetChanged();
        progressBar.setVisibility(View.GONE);
    }

    private void checkIfDriver() {
        progressBar.setVisibility(View.VISIBLE);
        firebaseDatabase.getReference("users").child(userId).child("driver").
                addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()) isDriver = true;
                        setSettings();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(
                                myContext,
                                "Failed to get the current user",
                                Toast.LENGTH_LONG
                        ).show();

                        setSettings();
                    }
                });
    }
}