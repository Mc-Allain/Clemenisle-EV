package com.example.firebase_clemenisle_ev.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.firebase_clemenisle_ev.Adapters.SettingsAdapter;
import com.example.firebase_clemenisle_ev.Classes.Setting;
import com.example.firebase_clemenisle_ev.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class SettingsFragment extends Fragment {

    RecyclerView settingsView;

    Context myContext;

    List<Setting> settingList = Arrays.asList(
            new Setting(R.drawable.ic_baseline_info_24, "About"),
            new Setting(R.drawable.ic_baseline_settings_24, "Preferences"),
            new Setting(R.drawable.ic_baseline_help_24, "Help"),
            new Setting(R.drawable.ic_baseline_electric_rickshaw_24, "Driver Mode"),
            new Setting(R.drawable.ic_baseline_logout_24, "Log out")
    );
    List<Setting> settings = new ArrayList<>();

    boolean isLoggedIn = false;

    private void initSharedPreferences() {
        SharedPreferences sharedPreferences = myContext
                .getSharedPreferences("login", Context.MODE_PRIVATE);
        isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        settingsView = view.findViewById(R.id.settingsView);

        myContext = getContext();

        initSharedPreferences();

        LinearLayoutManager linearLayout =
                new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        settingsView.setLayoutManager(linearLayout);

        for(Setting setting : settingList) {
            if(setting.getSettingName().equals("Log out")) {
                if(isLoggedIn) settings.add(setting);
            }
            else {
                settings.add(setting);
            }
        }

        SettingsAdapter settingsAdapter = new SettingsAdapter(myContext, settings);
        settingsView.setAdapter(settingsAdapter);

        return view;
    }
}