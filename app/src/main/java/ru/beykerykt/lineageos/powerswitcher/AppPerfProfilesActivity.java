package ru.beykerykt.lineageos.powerswitcher;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import static lineageos.power.PerformanceManager.PROFILE_HIGH_PERFORMANCE;
import static ru.beykerykt.lineageos.powerswitcher.Constants.APP_PREFERENCES_APPLICATIONS;
import static ru.beykerykt.lineageos.powerswitcher.Constants.APP_PREFERENCES_FIRST_LAUNCH_COMPLETE;
import static ru.beykerykt.lineageos.powerswitcher.Constants.APP_PREFERENCES_SERVICE_ENABLED;
import static ru.beykerykt.lineageos.powerswitcher.Constants.APP_PREFERENCES_SHOW_SYSTEM_APPS;
import static ru.beykerykt.lineageos.powerswitcher.Constants.getAppPrefs;

public class AppPerfProfilesActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context mContext, Intent intent) {
            String action = intent.getAction();

            if (action.equals(Intent.ACTION_PACKAGE_FULLY_REMOVED) || action.equals(Intent.ACTION_PACKAGE_ADDED)) {
                scheduleAppsLoad();
            }
        }
    };

    private LoadAppTask mTask = null;
    private RecyclerView mRecycleView;
    private RecyclerViewAdapter mAdapter;
    private TextView mTextView;
    private View mSwitchBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        // switch bar
        mTextView = findViewById(R.id.switch_text);
        mTextView.setText(getString(R.string.app_perf_profiles_title));

        boolean serviceEnabled = Constants.isServiceEnabled(this);
        mSwitchBar = findViewById(R.id.switch_bar);
        final Switch switchWidget = mSwitchBar.findViewById(android.R.id.switch_widget);
        switchWidget.setChecked(serviceEnabled);
        switchWidget.setOnCheckedChangeListener(this);
        mSwitchBar.setActivated(serviceEnabled);
        mSwitchBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchWidget.setChecked(!switchWidget.isChecked());
                mSwitchBar.setActivated(switchWidget.isChecked());
            }
        });

        // application list
        if (!getAppPrefs(this).getBoolean(APP_PREFERENCES_FIRST_LAUNCH_COMPLETE, false)) {
            // for test only
            Set<String> testApps = new HashSet<>();
            testApps.add("com.antutu.ABenchMark," + PROFILE_HIGH_PERFORMANCE);
            testApps.add("com.antutu.aibenchmark," + PROFILE_HIGH_PERFORMANCE);
            testApps.add("com.primatelabs.geekbench," + PROFILE_HIGH_PERFORMANCE);

            SharedPreferences.Editor e = getAppPrefs(this).edit();
            e.putStringSet(APP_PREFERENCES_APPLICATIONS, testApps);
            e.putBoolean(APP_PREFERENCES_FIRST_LAUNCH_COMPLETE, true);
            e.apply();
        }

        mRecycleView = findViewById(R.id.user_list_view);
        mRecycleView.setHasFixedSize(true);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this.getApplicationContext());
        mRecycleView.setLayoutManager(mLayoutManager);

        mAdapter = new RecyclerViewAdapter(this.getApplicationContext());
        mRecycleView.setAdapter(mAdapter);

        // start service
        if (getAppPrefs(this).getBoolean(APP_PREFERENCES_SERVICE_ENABLED, false)) {
            Intent intent = new Intent(this, PowerSwitcherService.class);
            startForegroundService(intent);
        }

        // register intent
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
        intentFilter.addAction(Intent.ACTION_PACKAGE_FULLY_REMOVED);
        intentFilter.addDataScheme("package");
        registerReceiver(mReceiver, intentFilter);

        // load apps
        scheduleAppsLoad();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAdapter.clearAppList();
        mRecycleView.setAdapter(null);
        mAdapter = null;
        mRecycleView = null;
        if (mTask != null) {
            mTask.cancel(true);
        }
        unregisterReceiver(mReceiver);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem checkable = menu.findItem(R.id.show_system_apps_checkbox);
        checkable.setChecked(shouldShowSystemApps());
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.show_system_apps_checkbox:
                boolean isChecked = !item.isChecked();
                item.setChecked(isChecked);
                SharedPreferences.Editor ed = getAppPrefs(this).edit();
                ed.putBoolean(APP_PREFERENCES_SHOW_SYSTEM_APPS, isChecked);
                ed.apply();
                scheduleAppsLoad();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        mSwitchBar.setActivated(isChecked);

        Intent intent = new Intent(this, PowerSwitcherService.class);
        if (isChecked) {
            startForegroundService(intent);
        } else {
            stopService(intent);
        }
        SharedPreferences.Editor editor = getAppPrefs(this).edit();
        editor.putBoolean(APP_PREFERENCES_SERVICE_ENABLED, isChecked);
        editor.apply();
    }

    private final String[] BLACKLISTED_PACKAGES = {
            "com.android.systemui"
    };

    private boolean isBlacklisted(String packageName) {
        for (String pkg : BLACKLISTED_PACKAGES) {
            if (pkg.equals(packageName)) {
                return true;
            }
        }
        return false;
    }

    private void scheduleAppsLoad() {
        mAdapter.clearAppList();
        mAdapter.notifyDataSetChanged();
        showProgressBar(true);
        if (mTask != null) {
            mTask.cancel(true);
        }
        mTask = new LoadAppTask(this);
        mTask.execute();
    }

    private boolean shouldShowSystemApps() {
        return getAppPrefs(this).getBoolean(APP_PREFERENCES_SHOW_SYSTEM_APPS, false);
    }

    public void showProgressBar(boolean flag) {
        ProgressBar progressBar = findViewById(R.id.progressBar);
        if (flag) {
            progressBar.setVisibility(View.VISIBLE);
        } else {
            progressBar.setVisibility(View.GONE);
        }
    }

    /**
     * An asynchronous task to load the icons of the installed applications.
     */
    private static class LoadAppTask extends AsyncTask<Void, Void, List<AppInfo>> {

        private WeakReference<AppPerfProfilesActivity> activityReference;

        LoadAppTask(AppPerfProfilesActivity context) {
            activityReference = new WeakReference<>(context);
        }

        @Override
        protected List<AppInfo> doInBackground(Void... voids) {
            List<AppInfo> appInfoList = new CopyOnWriteArrayList<>();

            AppPerfProfilesActivity activity = activityReference.get();
            if (activity == null || activity.isFinishing()) return appInfoList;

            List<PackageInfo> packages = activity.getPackageManager().getInstalledPackages(0);

            for (PackageInfo info : packages) {
                final ApplicationInfo appInfo = info.applicationInfo;

                // skip all system apps if they shall not be included
                if ((!activity.shouldShowSystemApps() && (appInfo.flags & (ApplicationInfo.FLAG_UPDATED_SYSTEM_APP | ApplicationInfo.FLAG_SYSTEM)) != 0)
                        || activity.getPackageManager().getLaunchIntentForPackage(info.packageName) == null && (appInfo.flags & (ApplicationInfo.FLAG_UPDATED_SYSTEM_APP | ApplicationInfo.FLAG_SYSTEM)) != 0
                        || activity.isBlacklisted(appInfo.packageName)) {
                    continue;
                }

                AppInfo app = new AppInfo();
                app.mAppLabel = appInfo.loadLabel(activity.getPackageManager()).toString();
                app.mPackageName = info.packageName;
                try {
                    Drawable icon = activity.getPackageManager().getApplicationIcon(app.mPackageName);
                    app.mIcon = icon;
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
                appInfoList.add(app);
            }
            // sort the apps by their enabled state, then by title
            Collections.sort(appInfoList);
            return appInfoList;
        }

        @Override
        protected void onPostExecute(List<AppInfo> appInfoList) {
            super.onPostExecute(appInfoList);
            AppPerfProfilesActivity activity = activityReference.get();
            if (activity == null || activity.isFinishing()) return;
            activity.mAdapter.setAppList(appInfoList);
            activity.showProgressBar(false);
        }
    }
}
