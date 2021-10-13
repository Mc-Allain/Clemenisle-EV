package com.example.firebase_clemenisle_ev.Classes;

public class Setting {

    private int settingIcon;
    private String settingName;

    public Setting(int settingIcon, String settingName) {
        this.settingIcon = settingIcon;
        this.settingName = settingName;
    }

    public int getSettingIcon() {
        return settingIcon;
    }

    public String getSettingName() {
        return settingName;
    }
}
