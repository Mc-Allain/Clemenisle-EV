package com.example.firebase_clemenisle_ev.Adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.firebase_clemenisle_ev.AboutActivity;
import com.example.firebase_clemenisle_ev.Classes.Setting;
import com.example.firebase_clemenisle_ev.DriverActivity;
import com.example.firebase_clemenisle_ev.HelpActivity;
import com.example.firebase_clemenisle_ev.MainActivity;
import com.example.firebase_clemenisle_ev.PreferenceActivity;
import com.example.firebase_clemenisle_ev.R;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

public class SettingsAdapter extends RecyclerView.Adapter<SettingsAdapter.ViewHolder> {

    List<Setting> settings;
    LayoutInflater inflater;

    Context myContext;
    Resources myResources;

    long logoutPressedTime;
    Toast logoutToast;

    private void sendLoginPreferences() {
        SharedPreferences sharedPreferences = myContext.getSharedPreferences(
                "login", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putBoolean("isLoggedIn", false);
        editor.putBoolean("remember", false);
        editor.putString("emailAddress", null);
        editor.putString("password", null);
        editor.putBoolean("inDriverModule", false);

        editor.apply();
    }

    private void sendDriverModePreferences(boolean value) {
        SharedPreferences sharedPreferences = myContext.getSharedPreferences(
                "login", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putBoolean("inDriverModule", value);
        editor.putBoolean("remember", true);
        editor.apply();
    }

    public SettingsAdapter(Context context, List<Setting> settings) {
        this.settings = settings;
        this.inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.custom_setting_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TextView tvSettingName = holder.tvSettingName;
        ImageView settingIconImage = holder.settingIconImage;
        ConstraintLayout settingLayout = holder.settingLayout;

        myContext = inflater.getContext();
        myResources = myContext.getResources();

        int colorRed = myResources.getColor(R.color.red);
        int colorBlack = myResources.getColor(R.color.black);

        int settingIcon = settings.get(position).getSettingIcon();
        String settingName = settings.get(position).getSettingName();

        settingIconImage.setImageResource(settingIcon);
        tvSettingName.setText(settingName);

        if(settingName.equals("Log out") || settingName.equals("Exit Driver Module")) {
            settingIconImage.getDrawable().setTint(colorRed);
            tvSettingName.setTextColor(colorRed);
        }
        else {
            settingIconImage.getDrawable().setTint(colorBlack);
            tvSettingName.setTextColor(colorBlack);
        }

        int bottom = dpToPx(4);

        boolean isLastItem = position + 1 == getItemCount();

        if(isLastItem) {
            bottom = dpToPx(88);
        }

        ConstraintLayout.LayoutParams layoutParams =
                (ConstraintLayout.LayoutParams) settingLayout.getLayoutParams();
        layoutParams.setMargins(layoutParams.leftMargin, layoutParams.topMargin,
                layoutParams.rightMargin, bottom);
        settingLayout.setLayoutParams(layoutParams);

        settingLayout.setOnClickListener(view -> {
            switch (settingName) {
                case "Log out":
                    if (logoutPressedTime + 2500 > System.currentTimeMillis()) {
                        logoutToast.cancel();
                        sendLoginPreferences();

                        Intent intent = new Intent(myContext, MainActivity.class);
                        myContext.startActivity(intent);
                        ((Activity) myContext).finishAffinity();
                    } else {
                        logoutToast = Toast.makeText(myContext,
                                "Press again to log out", Toast.LENGTH_SHORT);
                        logoutToast.show();
                    }

                    logoutPressedTime = System.currentTimeMillis();
                    break;
                case "About": {
                    Intent intent = new Intent(myContext, AboutActivity.class);
                    myContext.startActivity(intent);
                    break;
                }
                case "Help": {
                    Intent intent = new Intent(myContext, HelpActivity.class);
                    myContext.startActivity(intent);
                    break;
                }
                case "Preferences": {
                    Intent intent = new Intent(myContext, PreferenceActivity.class);
                    myContext.startActivity(intent);
                    break;
                }
                case "Driver Module": {
                    sendDriverModePreferences(true);

                    Intent intent = new Intent(myContext, DriverActivity.class);
                    myContext.startActivity(intent);
                    ((Activity) myContext).finishAffinity();
                    break;
                }
                case "Exit Driver Module": {
                    sendDriverModePreferences(false);

                    Toast.makeText(
                            myContext,
                            "You exited the Driver Module",
                            Toast.LENGTH_LONG
                    ).show();

                    Intent intent = new Intent(myContext, MainActivity.class);
                    myContext.startActivity(intent);
                    ((Activity) myContext).finishAffinity();
                    break;
                }
            }
        });
    }

    private int dpToPx(int dp) {
        float px = dp * myContext.getResources().getDisplayMetrics().density;
        return (int) px;
    }

    @Override
    public int getItemCount() {
        return settings.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvSettingName;
        ImageView settingIconImage;
        ConstraintLayout settingLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSettingName = itemView.findViewById(R.id.tvSettingName);
            settingIconImage = itemView.findViewById(R.id.settingIconImage);
            settingLayout = itemView.findViewById(R.id.settingLayout);
        }
    }
}
