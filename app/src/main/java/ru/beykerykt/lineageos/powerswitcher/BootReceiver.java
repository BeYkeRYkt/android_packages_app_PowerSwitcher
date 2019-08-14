package ru.beykerykt.lineageos.powerswitcher;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import static ru.beykerykt.lineageos.powerswitcher.Constants.APP_PREFERENCES_SERVICE_ENABLED;
import static ru.beykerykt.lineageos.powerswitcher.Constants.getAppPrefs;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        boolean isServiceEnabled = getAppPrefs(context).getBoolean(APP_PREFERENCES_SERVICE_ENABLED, false);
        if (isServiceEnabled) {
            Intent startServiceIntent = new Intent(context, PowerSwitcherService.class);
            context.startForegroundService(startServiceIntent);
        }
    }
}