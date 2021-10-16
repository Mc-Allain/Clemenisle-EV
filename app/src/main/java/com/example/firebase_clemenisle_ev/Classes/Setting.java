package com.example.firebase_clemenisle_ev.Classes;

public class Setting {

    private final int settingIcon;
    private final String settingName;

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
