package com.example.firebase_clemenisle_ev;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;

import com.google.android.material.switchmaterial.SwitchMaterial;

import androidx.appcompat.app.AppCompatActivity;

public class PreferenceActivity extends AppCompatActivity {

    SwitchMaterial swBookingAlert, swAppVersionInfo, swBookingOptionDialog, swConfirmationDialog;

    Context myContext;
    Resources myResources;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    boolean isShowAppVersionInfoEnabled, isShowBookingAlertEnabled, isBookingOptionDialogEnabled, isConfirmationDialogEnabled;

    private void initSharedPreferences() {
        sharedPreferences = myContext.getSharedPreferences(
                "preferences", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        getSharedPreferences("preferences", Context.MODE_PRIVATE);
        isShowBookingAlertEnabled = sharedPreferences.getBoolean("isShowBookingAlertEnabled", true);
        isShowAppVersionInfoEnabled = sharedPreferences.getBoolean("isShowAppVersionInfoEnabled", true);
        isBookingOptionDialogEnabled = sharedPreferences.getBoolean("isBookingOptionDialogEnabled", true);
        isConfirmationDialogEnabled = sharedPreferences.getBoolean("isConfirmationDialogEnabled", true);
    }
    private void sendIsShowBookingAlertEnabledPreferences(boolean value) {
        editor.putBoolean("isShowBookingAlertEnabled", value);
        editor.apply();
    }

    private void sendIsShowAppVersionInfoEnabledPreferences(boolean value) {
        editor.putBoolean("isShowAppVersionInfoEnabled", value);
        editor.apply();
    }

    private void sendIsBookingOptionDialogEnabledPreferences(boolean value) {
        editor.putBoolean("isBookingOptionDialogEnabled", value);
        editor.apply();
    }

    private void sendIsConfirmationDialogEnabledPreferences(boolean value) {
        editor.putBoolean("isConfirmationDialogEnabled", value);
        editor.apply();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preference);

        swBookingAlert = findViewById(R.id.swBookingAlert);
        swAppVersionInfo = findViewById(R.id.swAppVersionInfo);
        swBookingOptionDialog = findViewById(R.id.swBookingOptionDialog);
        swConfirmationDialog = findViewById(R.id.swConfirmationDialog);

        myContext = PreferenceActivity.this;
        myResources = myContext.getResources();

        initSharedPreferences();

        swBookingAlert.setChecked(isShowBookingAlertEnabled);
        swAppVersionInfo.setChecked(isShowAppVersionInfoEnabled);
        swBookingOptionDialog.setChecked(isBookingOptionDialogEnabled);
        swConfirmationDialog.setChecked(isConfirmationDialogEnabled);

        swBookingAlert.setOnClickListener(view -> sendIsShowBookingAlertEnabledPreferences(swBookingAlert.isChecked()));
        swAppVersionInfo.setOnClickListener(view -> sendIsShowAppVersionInfoEnabledPreferences(swAppVersionInfo.isChecked()));
        swBookingOptionDialog.setOnClickListener(view -> sendIsBookingOptionDialogEnabledPreferences(swBookingOptionDialog.isChecked()));
        swConfirmationDialog.setOnClickListener(view -> sendIsConfirmationDialogEnabledPreferences(swConfirmationDialog.isChecked()));
    }
}