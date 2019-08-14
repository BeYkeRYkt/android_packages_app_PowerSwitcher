package ru.beykerykt.lineageos.powerswitcher.ui;

import android.graphics.drawable.Drawable;

public class AppInfo implements Comparable<AppInfo> {
    public String mAppLabel;
    public String mPackageName;
    public Drawable mIcon;

    @Override
    public int compareTo(AppInfo another) {
        return mAppLabel.compareToIgnoreCase(another.mAppLabel);
    }
}
