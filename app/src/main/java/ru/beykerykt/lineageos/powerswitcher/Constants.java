package ru.beykerykt.lineageos.powerswitcher;

import android.content.Context;
import android.content.SharedPreferences;

public class Constants {

    // SharedPreferences
    public static final String APP_PREFERENCES = "powerSwitcher";
    public static final String APP_PREFERENCES_APPLICATIONS = "applications";
    public static final String APP_PREFERENCES_DEFAULT_POWER_PROFILE = "defaultPowerProfile";
    public static final String APP_PREFERENCES_FIRST_LAUNCH_COMPLETE = "firstLaunchComplete";
    public static final String APP_PREFERENCES_SERVICE_ENABLED = "serviceEnabled";
    public static final String APP_PREFERENCES_SHOW_SYSTEM_APPS = "showSystemApps";

    // notification
    public static final String NOTIFICATION_CHANNEL_ID = "PowerSwitcherServiceChannel";
    public static final int NOTIFICATION_NOTIFY_ID = 10001;

    public static SharedPreferences getAppPrefs(Context context) {
        return context.getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
    }

    public static boolean isServiceEnabled(Context context) {
        return getAppPrefs(context).getBoolean(APP_PREFERENCES_SERVICE_ENABLED, false);
    }
}
