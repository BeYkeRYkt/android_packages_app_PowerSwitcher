package ru.beykerykt.lineageos.powerswitcher;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.util.Log;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;

import static ru.beykerykt.lineageos.powerswitcher.Constants.APP_PREFERENCES_APPLICATIONS;
import static ru.beykerykt.lineageos.powerswitcher.Constants.getAppPrefs;

public class PerfProfilesManager {

    private String TAG = "PerfProfilesManager";

    private static PerfProfilesManager perfProfilesManager;
    private static final LinkedHashMap<String, Integer> mAppProfiles = new LinkedHashMap<>(); // for tests

    public PerfProfilesManager() {
    }

    public static PerfProfilesManager getInstance() {
        if (perfProfilesManager != null) {
            return perfProfilesManager;
        }
        perfProfilesManager = new PerfProfilesManager();
        return perfProfilesManager;
    }

    public LinkedHashMap<String, Integer> getAppProfiles() {
        return mAppProfiles;
    }

    public void addAppProfile(String pkgName, int profileId) {
        if (!mAppProfiles.containsKey(pkgName)) {
            mAppProfiles.put(pkgName, profileId);
            Log.i(TAG, "Added new profile for app=" + pkgName + " profileId=" + profileId);
        } else {
            if (mAppProfiles.get(pkgName) != profileId) {
                mAppProfiles.replace(pkgName, profileId);
                Log.i(TAG, "Update profile for app=" + pkgName + " profileId=" + profileId);
            }
        }
    }

    public void removeAppProfile(String pkgName) {
        if (availableAppProfile(pkgName)) {
            mAppProfiles.remove(pkgName);
            Log.i(TAG, "Remove profile for app=" + pkgName);
        }
    }

    public boolean availableAppProfile(String pkg) {
        boolean flag = false;
        if (pkg == null) return flag;
        if (mAppProfiles.containsKey(pkg)) {
            flag = true;
        }
        return flag;
    }

    /**
     * Get specific power profile from app package name
     */
    public int getProfileFromAppPackage(String pkg) {
        int profile = -1;
        if (pkg == null) return profile;
        if (mAppProfiles.containsKey(pkg)) {
            profile = mAppProfiles.get(pkg);
        }
        return profile;
    }

    private boolean isAppInstalled(Context context, String packageName) {
        try {
            context.getPackageManager().getApplicationInfo(packageName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public void restoreAppProfiles(Context context) {
        String[] applications = getAppPrefs(context).getStringSet(APP_PREFERENCES_APPLICATIONS, new HashSet<String>()).toArray(new String[0]);
        if (applications != null && applications.length > 0) {
            Log.i(TAG, "Restoring per-app profiles");
            for (int i = 0; i < applications.length; i++) {
                String[] info = applications[i].split(",");
                if (info.length == 2) {
                    if (isAppInstalled(context, info[0])) {
                        if (!mAppProfiles.containsKey(info[0])) {
                            mAppProfiles.put(info[0], Integer.valueOf(info[1]));
                        }
                    }
                }
            }
        }
    }

    public void saveAppProfiles(Context context) {
        SharedPreferences pref = getAppPrefs(context);
        Set<String> applications = new HashSet<>();
        Log.i(TAG, "Saving per-app profiles");
        for (String pkgName : getAppProfiles().keySet()) {
            int profileId = getAppProfiles().get(pkgName);
            String line = pkgName + "," + profileId;
            applications.add(line);
        }
        SharedPreferences.Editor ed = pref.edit();
        ed.putStringSet(APP_PREFERENCES_APPLICATIONS, applications);
        ed.apply();
    }
}
