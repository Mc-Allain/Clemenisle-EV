package com.example.firebase_clemenisle_ev.Fragments;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.transition.ChangeBounds;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.firebase_clemenisle_ev.R;
import com.google.android.material.switchmaterial.SwitchMaterial;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.fragment.app.Fragment;

public class MapSettingsFragment extends Fragment {

    TextView tvTSMarkColor;
    ImageView tsColorImage, tsBlueImage, tsRedImage, tsGreenImage, tsOrangeImage, tsDarkVioletImage, tsBlackImage;
    ConstraintLayout tsColorLayout, tsColorsLayout;

    TextView tvSMarkColor;
    ImageView sColorImage, sBlueImage, sRedImage, sGreenImage, sOrangeImage, sDarkVioletImage, sBlackImage;
    ConstraintLayout sColorLayout, sColorsLayout;

    TextView tvTSMarkIcon, tvTSDefault, tvTSMapPin;
    ImageView tsDefaultImage, tsMapPinImage;

    TextView tvSMarkIcon, tvSDefault, tvSMapPin;
    ImageView sDefaultImage, sMapPinImage;

    TextView tvMapType, tvMSDefault, tvMSSat;
    ImageView msDefaultImage, msSatImage;

    SwitchMaterial swMapAutoFocus;

    Context myContext;
    Resources myResources;

    OnMapSettingChangeListener onMapSettingChangeListener;

    int tsMarkColor, sMarkColor, tsMarkIcon, sMarkIcon, mapType;
    boolean mapAutoFocus;
    int colorBlue, colorInitial;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map_settings, container, false);

        tvTSMarkColor = view.findViewById(R.id.tvTSMarkColor);
        tsColorImage = view.findViewById(R.id.tsColorImage);
        tsBlueImage = view.findViewById(R.id.tsBlueImage);
        tsRedImage = view.findViewById(R.id.tsRedImage);
        tsGreenImage = view.findViewById(R.id.tsGreenImage);
        tsOrangeImage = view.findViewById(R.id.tsOrangeImage);
        tsDarkVioletImage = view.findViewById(R.id.tsDarkVioletImage);
        tsBlackImage = view.findViewById(R.id.tsBlackImage);
        tsColorLayout = view.findViewById(R.id.tsColorLayout);
        tsColorsLayout = view.findViewById(R.id.tsColorsLayout);

        tvSMarkColor = view.findViewById(R.id.tvSMarkColor);
        sColorImage = view.findViewById(R.id.sColorImage);
        sBlueImage = view.findViewById(R.id.sBlueImage);
        sRedImage = view.findViewById(R.id.sRedImage);
        sGreenImage = view.findViewById(R.id.sGreenImage);
        sOrangeImage = view.findViewById(R.id.sOrangeImage);
        sDarkVioletImage = view.findViewById(R.id.sDarkVioletImage);
        sBlackImage = view.findViewById(R.id.sBlackImage);
        sColorLayout = view.findViewById(R.id.sColorLayout);
        sColorsLayout = view.findViewById(R.id.sColorsLayout);

        tvTSMarkIcon = view.findViewById(R.id.tvTSMarkIcon);
        tvTSDefault = view.findViewById(R.id.tvTSDefault);
        tvTSMapPin = view.findViewById(R.id.tvTSMapPin);
        tsDefaultImage = view.findViewById(R.id.tsDefaultImage);
        tsMapPinImage = view.findViewById(R.id.tsMapPinImage);

        tvSMarkIcon = view.findViewById(R.id.tvSMarkIcon);
        tvSDefault = view.findViewById(R.id.tvSDefault);
        tvSMapPin = view.findViewById(R.id.tvSMapPin);
        sDefaultImage = view.findViewById(R.id.sDefaultImage);
        sMapPinImage = view.findViewById(R.id.sMapPinImage);

        tvMapType = view.findViewById(R.id.tvMapType);
        tvMSDefault = view.findViewById(R.id.tvMSDefault);
        tvMSSat = view.findViewById(R.id.tvMSSat);
        msDefaultImage = view.findViewById(R.id.msDefaultImage);
        msSatImage = view.findViewById(R.id.msSatImage);

        swMapAutoFocus = view.findViewById(R.id.swMapAutoFocus);

        myContext = getContext();
        myResources = myContext.getResources();

        colorBlue = myResources.getColor(R.color.blue);
        colorInitial = myResources.getColor(R.color.initial);

        initSharedPreferences();
        changeSelectedTSColor();
        changeSelectedSColor();
        changeTSMarkIcon();
        changeSMarkIcon();
        changeMapType();
        changeMapAutoFocus();

        tsColorLayout.setOnClickListener(view1 -> openTSColorLayout());

        tsBlueImage.setOnClickListener(view1 -> {
            tsMarkColor = myResources.getColor(R.color.blue);
            setTSColor(1);
        });

        tsRedImage.setOnClickListener(view1 -> {
            tsMarkColor = myResources.getColor(R.color.red);
            setTSColor(2);
        });

        tsGreenImage.setOnClickListener(view1 -> {
            tsMarkColor = myResources.getColor(R.color.green);
            setTSColor(3);
        });

        tsOrangeImage.setOnClickListener(view1 -> {
            tsMarkColor = myResources.getColor(R.color.orange);
            setTSColor(4);
        });

        tsDarkVioletImage.setOnClickListener(view1 -> {
            tsMarkColor = myResources.getColor(R.color.dark_violet);
            setTSColor(5);
        });

        tsBlackImage.setOnClickListener(view1 -> {
            tsMarkColor = myResources.getColor(R.color.black);
            setTSColor(6);
        });

        sColorLayout.setOnClickListener(view1 -> openSColorLayout());

        sBlueImage.setOnClickListener(view1 -> {
            sMarkColor = myResources.getColor(R.color.blue);
            setSColor(1);
        });

        sRedImage.setOnClickListener(view1 -> {
            sMarkColor = myResources.getColor(R.color.red);
            setSColor(2);
        });

        sGreenImage.setOnClickListener(view1 -> {
            sMarkColor = myResources.getColor(R.color.green);
            setSColor(3);
        });

        sOrangeImage.setOnClickListener(view1 -> {
            sMarkColor = myResources.getColor(R.color.orange);
            setSColor(4);
        });

        sDarkVioletImage.setOnClickListener(view1 -> {
            sMarkColor = myResources.getColor(R.color.dark_violet);
            setSColor(5);
        });

        sBlackImage.setOnClickListener(view1 -> {
            sMarkColor = myResources.getColor(R.color.black);
            setSColor(6);
        });

        tvTSDefault.setOnClickListener(view1 -> {
            setTSDefault();
        });

        tsDefaultImage.setOnClickListener(view1 -> {
            setTSDefault();
        });

        tvTSMapPin.setOnClickListener(view1 -> {
            setTSMapPin();
        });

        tsMapPinImage.setOnClickListener(view1 -> {
            setTSMapPin();
        });

        tvSDefault.setOnClickListener(view1 -> {
            setSDefault();
        });

        sDefaultImage.setOnClickListener(view1 -> {
            setSDefault();
        });

        tvSMapPin.setOnClickListener(view1 -> {
            setSMapPin();
        });

        sMapPinImage.setOnClickListener(view1 -> {
            setSMapPin();
        });

        tvMSDefault.setOnClickListener(view1 -> {
            setMSDefault();
        });

        msDefaultImage.setOnClickListener(view1 -> {
            setMSDefault();
        });

        tvMSSat.setOnClickListener(view1 -> {
            setMSSat();
        });

        msSatImage.setOnClickListener(view1 -> {
            setMSSat();
        });

        swMapAutoFocus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mapAutoFocus = swMapAutoFocus.isChecked();
                setMapAutoFocus();
            }
        });

        return view;
    }

    private void setTSDefault() {
        tsMarkIcon = R.drawable.ic_baseline_tour_24;
        changeTSMarkIcon();
        sendTSMarkIconPreferences(tsMarkIcon);
        onMapSettingChangeListener.sendMarkIcon(tsMarkIcon, 0);
    }

    private void setTSMapPin() {
        tsMarkIcon = R.drawable.ic_baseline_location_on_24;
        changeTSMarkIcon();
        sendTSMarkIconPreferences(tsMarkIcon);
        onMapSettingChangeListener.sendMarkIcon(tsMarkIcon, 0);
    }

    private void changeTSMarkIcon() {
        tvTSDefault.setTextColor(colorInitial);
        tsDefaultImage.setColorFilter(colorInitial);
        tvTSMapPin.setTextColor(colorInitial);
        tsMapPinImage.setColorFilter(colorInitial);

        if(tsMarkIcon == R.drawable.ic_baseline_tour_24) {
            tvTSDefault.setTextColor(colorBlue);
            tsDefaultImage.setColorFilter(colorBlue);
        }
        else {
            tvTSMapPin.setTextColor(colorBlue);
            tsMapPinImage.setColorFilter(colorBlue);
        }
    }

    private void setSDefault() {
        sMarkIcon = R.drawable.ic_baseline_ev_station_24;
        changeSMarkIcon();
        sendSMarkIconPreferences(sMarkIcon);
        onMapSettingChangeListener.sendMarkIcon(sMarkIcon, 1);
    }

    private void setSMapPin() {
        sMarkIcon = R.drawable.ic_baseline_location_on_24;
        changeSMarkIcon();
        sendSMarkIconPreferences(sMarkIcon);
        onMapSettingChangeListener.sendMarkIcon(sMarkIcon, 1);
    }

    private void changeSMarkIcon() {
        tvSDefault.setTextColor(colorInitial);
        sDefaultImage.setColorFilter(colorInitial);
        tvSMapPin.setTextColor(colorInitial);
        sMapPinImage.setColorFilter(colorInitial);

        if(sMarkIcon == R.drawable.ic_baseline_ev_station_24) {
            tvSDefault.setTextColor(colorBlue);
            sDefaultImage.setColorFilter(colorBlue);
        }
        else {
            tvSMapPin.setTextColor(colorBlue);
            sMapPinImage.setColorFilter(colorBlue);
        }
    }

    private void setMSDefault() {
        mapType = 1;
        changeMapType();
        sendMapTypePreferences(mapType);
        onMapSettingChangeListener.sendMapType(mapType);
    }

    private void setMSSat() {
        mapType = 2;
        changeMapType();
        sendMapTypePreferences(mapType);
        onMapSettingChangeListener.sendMapType(mapType);
    }

    private void changeMapType() {
        tvMSDefault.setTextColor(colorInitial);
        msDefaultImage.setColorFilter(colorInitial);
        tvMSSat.setTextColor(colorInitial);
        msSatImage.setColorFilter(colorInitial);

        if(mapType == 1) {
            tvMSDefault.setTextColor(colorBlue);
            msDefaultImage.setColorFilter(colorBlue);
        }
        else if(mapType == 2) {
            tvMSSat.setTextColor(colorBlue);
            msSatImage.setColorFilter(colorBlue);
        }
    }

    private void setMapAutoFocus() {
        changeMapAutoFocus();
        sendMapAutoFocusPreferences(mapAutoFocus);
        onMapSettingChangeListener.sendMapAutoFocus(mapAutoFocus);
    }

    private void changeMapAutoFocus() {
        swMapAutoFocus.setChecked(mapAutoFocus);
    }

    public interface OnMapSettingChangeListener {
        void sendMarkColor(int color, int type);
        void sendMarkIcon(int icon, int type);
        void sendMapType(int type);
        void sendMapAutoFocus(boolean value);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        Activity activity = (Activity) context;

        try {
            onMapSettingChangeListener = (OnMapSettingChangeListener) activity;
        }
        catch(Exception exception) {
            Toast.makeText(
                    context,
                    "Interface error",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    private void setTSColor(int color) {
        changeSelectedTSColor();
        sendTSMarkColorPreferences(color);
        closeTSColorLayout();
        onMapSettingChangeListener.sendMarkColor(tsMarkColor, 0);
    }

    private void changeSelectedTSColor() {
        tsColorImage.setColorFilter(tsMarkColor);
        tsColorImage.setBackgroundColor(tsMarkColor);
    }

    private void openTSColorLayout() {
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(tsColorLayout);

        constraintSet.connect(tsColorsLayout.getId(), ConstraintSet.START,
                tvTSMarkColor.getId(), ConstraintSet.END);

        setTransition(tsColorsLayout);
        constraintSet.applyTo(tsColorLayout);

        setTSColorImagesVisibility(View.VISIBLE);
        tsColorImage.setVisibility(View.INVISIBLE);
    }

    private void closeTSColorLayout() {
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(tsColorLayout);

        constraintSet.clear(tsColorsLayout.getId(), ConstraintSet.START);
        constraintSet.applyTo(tsColorLayout);

        setTSColorImagesVisibility(View.GONE);
        tsColorImage.setVisibility(View.VISIBLE);
    }

    private void setTransition(ConstraintLayout constraintLayout) {
        Transition transition = new ChangeBounds();
        transition.setDuration(300);
        TransitionManager.beginDelayedTransition(constraintLayout, transition);
    }

    private void setTSColorImagesVisibility(int value) {
        tsBlueImage.setVisibility(value);
        tsRedImage.setVisibility(value);
        tsGreenImage.setVisibility(value);
        tsOrangeImage.setVisibility(value);
        tsDarkVioletImage.setVisibility(value);
        tsBlackImage.setVisibility(value);
    }

    private void setSColor(int color) {
        changeSelectedSColor();
        sendSMarkColorPreferences(color);
        closeSColorLayout();
        onMapSettingChangeListener.sendMarkColor(sMarkColor, 1);
    }

    private void changeSelectedSColor() {
        sColorImage.setColorFilter(sMarkColor);
        sColorImage.setBackgroundColor(sMarkColor);
    }

    private void openSColorLayout() {
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(sColorLayout);

        constraintSet.connect(sColorsLayout.getId(), ConstraintSet.START,
                tvSMarkColor.getId(), ConstraintSet.END);

        setTransition(sColorsLayout);
        constraintSet.applyTo(sColorLayout);

        setSColorImagesVisibility(View.VISIBLE);
        sColorImage.setVisibility(View.INVISIBLE);
    }

    private void closeSColorLayout() {
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(sColorLayout);

        constraintSet.clear(sColorsLayout.getId(), ConstraintSet.START);
        constraintSet.applyTo(sColorLayout);

        setSColorImagesVisibility(View.GONE);
        sColorImage.setVisibility(View.VISIBLE);
    }

    private void setSColorImagesVisibility(int value) {
        sBlueImage.setVisibility(value);
        sRedImage.setVisibility(value);
        sGreenImage.setVisibility(value);
        sOrangeImage.setVisibility(value);
        sDarkVioletImage.setVisibility(value);
        sBlackImage.setVisibility(value);
    }

    private void sendTSMarkColorPreferences(int value) {
        SharedPreferences sharedPreferences = myContext
                .getSharedPreferences("mapSetting", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putInt("tsColorCode", value);
        editor.apply();
    }

    private void sendSMarkColorPreferences(int value) {
        SharedPreferences sharedPreferences = myContext
                .getSharedPreferences("mapSetting", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putInt("sColorCode", value);
        editor.apply();
    }

    private void sendTSMarkIconPreferences(int value) {
        SharedPreferences sharedPreferences = myContext
                .getSharedPreferences("mapSetting", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putInt("tsMarkIcon", value);
        editor.apply();
    }

    private void sendSMarkIconPreferences(int value) {
        SharedPreferences sharedPreferences = myContext
                .getSharedPreferences("mapSetting", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putInt("sMarkIcon", value);
        editor.apply();
    }

    private void sendMapTypePreferences(int value) {
        SharedPreferences sharedPreferences = myContext
                .getSharedPreferences("mapSetting", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putInt("mapType", value);
        editor.apply();
    }

    private void sendMapAutoFocusPreferences(boolean value) {
        SharedPreferences sharedPreferences = myContext
                .getSharedPreferences("mapSetting", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putBoolean("mapAutoFocus", value);
        editor.apply();
    }

    private void initSharedPreferences() {
        SharedPreferences sharedPreferences = myContext
                .getSharedPreferences("mapSetting", Context.MODE_PRIVATE);

        int tsColorCode = sharedPreferences.getInt("tsColorCode", 1);

        switch (tsColorCode) {
            case 1:
                tsMarkColor = myResources.getColor(R.color.blue);
                break;
            case 2:
                tsMarkColor = myResources.getColor(R.color.red);
                break;
            case 3:
                tsMarkColor = myResources.getColor(R.color.green);
                break;
            case 4:
                tsMarkColor = myResources.getColor(R.color.orange);
                break;
            case 5:
                tsMarkColor = myResources.getColor(R.color.dark_violet);
                break;
            case 6:
                tsMarkColor = myResources.getColor(R.color.black);
                break;
        }

        int sColorCode = sharedPreferences.getInt("sColorCode", 1);

        switch (sColorCode) {
            case 1:
                sMarkColor = myResources.getColor(R.color.blue);
                break;
            case 2:
                sMarkColor = myResources.getColor(R.color.red);
                break;
            case 3:
                sMarkColor = myResources.getColor(R.color.green);
                break;
            case 4:
                sMarkColor = myResources.getColor(R.color.orange);
                break;
            case 5:
                sMarkColor = myResources.getColor(R.color.dark_violet);
                break;
            case 6:
                sMarkColor = myResources.getColor(R.color.black);
                break;
        }

        tsMarkIcon = sharedPreferences.getInt("tsMarkIcon", R.drawable.ic_baseline_tour_24);
        sMarkIcon = sharedPreferences.getInt("sMarkIcon", R.drawable.ic_baseline_ev_station_24);
        mapType = sharedPreferences.getInt("mapType", 1);
        mapAutoFocus = sharedPreferences.getBoolean("mapAutoFocus", true);
    }
}