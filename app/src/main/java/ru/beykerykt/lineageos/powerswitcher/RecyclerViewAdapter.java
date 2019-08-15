package ru.beykerykt.lineageos.powerswitcher;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.internal.widget.PreferenceImageView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static lineageos.power.PerformanceManager.PROFILE_BALANCED;
import static lineageos.power.PerformanceManager.PROFILE_BIAS_PERFORMANCE;
import static lineageos.power.PerformanceManager.PROFILE_BIAS_POWER_SAVE;
import static lineageos.power.PerformanceManager.PROFILE_HIGH_PERFORMANCE;
import static lineageos.power.PerformanceManager.PROFILE_POWER_SAVE;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements AdapterView.OnItemSelectedListener {

    private List<AppInfo> mApps;
    private Context mContext;
    private Drawable mDefaultImg;
    private final PerfProfilesManager mPerfProfilesManager;

    private int TYPE_HEADER = 0;
    private int TYPE_ITEM = 1;

    private int PROFILE_NOTHING = -1;

    public RecyclerViewAdapter(Context context) {
        this.mContext = context.getApplicationContext();
        this.mApps = new CopyOnWriteArrayList<>();
        this.mPerfProfilesManager = PerfProfilesManager.getInstance();
        if (mPerfProfilesManager.getAppProfiles().isEmpty()) {
            mPerfProfilesManager.restoreAppProfiles(mContext);
        }
        mDefaultImg = mContext.getDrawable(android.R.mipmap.sym_def_app_icon);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView;
        if (i == TYPE_ITEM) {
            itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.app_list_item, viewGroup, false);
            AppViewHolder appViewHolder = new AppViewHolder(itemView);
            return appViewHolder;
        } else if (i == TYPE_HEADER) {
            itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.app_list_header, viewGroup, false);
            HeaderViewHolder headerViewHolder = new HeaderViewHolder(itemView);
            return headerViewHolder;
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        if (viewHolder instanceof AppViewHolder) {
            AppViewHolder appViewHolder = (AppViewHolder) viewHolder;
            AppInfo data = getItem(i);
            appViewHolder.title.setText(data.mAppLabel);
            Drawable icon = mDefaultImg;
            if (data.mIcon != null) {
                icon = data.mIcon;
            }
            appViewHolder.icon.setImageDrawable(icon);
            appViewHolder.mode.setAdapter(new PerfProfileSpinnerAdapter(mContext));
            appViewHolder.mode.setTag(data);
            int profileId = 0;
            if (mPerfProfilesManager.availableAppProfile(data.mPackageName)) {
                profileId = convertProfileToItemList(mPerfProfilesManager.getProfileFromAppPackage(data.mPackageName));
            }
            appViewHolder.mode.setSelection(profileId);
            appViewHolder.mode.setOnItemSelectedListener(this);
        } else if (viewHolder instanceof HeaderViewHolder) {
            HeaderViewHolder headerViewHolder = (HeaderViewHolder) viewHolder;
            headerViewHolder.summary.setText(R.string.app_perf_profiles_summary);
            headerViewHolder.icon.setImageResource(R.drawable.ic_info);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (isPositionHeader(position))
            return TYPE_HEADER;
        return TYPE_ITEM;
    }

    private AppInfo getItem(int position) {
        return mApps.get(position - 1);
    }

    @Override
    public int getItemCount() {
        return mApps.size() + 1;
    }

    private boolean isPositionHeader(int position) {
        return position == 0;
    }

    private int convertProfileToItemList(int rawLineagePowerProfile) {
        int checkedItem = 0;
        switch (rawLineagePowerProfile) {
            case PROFILE_POWER_SAVE:
                checkedItem = 1;
                break;
            case PROFILE_BIAS_POWER_SAVE:
                checkedItem = 2;
                break;
            case PROFILE_BALANCED:
                checkedItem = 3;
                break;
            case PROFILE_BIAS_PERFORMANCE:
                checkedItem = 4;
                break;
            case PROFILE_HIGH_PERFORMANCE:
                checkedItem = 5;
                break;
        }
        return checkedItem;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String pkgName = ((AppInfo) parent.getTag()).mPackageName;
        int profile = PROFILE_NOTHING;
        switch (position) {
            case 1:
                profile = PROFILE_POWER_SAVE;
                break;
            case 2:
                profile = PROFILE_BIAS_POWER_SAVE;
                break;
            case 3:
                profile = PROFILE_BALANCED;
                break;
            case 4:
                profile = PROFILE_BIAS_PERFORMANCE;
                break;
            case 5:
                profile = PROFILE_HIGH_PERFORMANCE;
                break;
        }
        if (profile != PROFILE_NOTHING) {
            mPerfProfilesManager.addAppProfile(pkgName, profile);
        } else {
            mPerfProfilesManager.removeAppProfile(pkgName);
        }
        mPerfProfilesManager.saveAppProfiles(mContext);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    public void clearAppList() {
        this.mApps.clear();
    }

    public void setAppList(List<AppInfo> appInfoList) {
        clearAppList();
        this.mApps = appInfoList;
        notifyDataSetChanged();
    }

    private static class AppViewHolder extends RecyclerView.ViewHolder {

        public TextView title;
        public Spinner mode;
        public PreferenceImageView icon;

        public AppViewHolder(@NonNull View itemView) {
            super(itemView);
            this.title = (TextView) itemView.findViewById(R.id.app_name);
            this.mode = (Spinner) itemView.findViewById(R.id.app_perf_profile);
            this.icon = (PreferenceImageView) itemView.findViewById(R.id.app_icon);
        }
    }

    private static class HeaderViewHolder extends RecyclerView.ViewHolder {

        public TextView summary;
        public PreferenceImageView icon;

        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            this.summary = (TextView) itemView.findViewById(R.id.header_summary);
            this.icon = (PreferenceImageView) itemView.findViewById(R.id.header_icon);
        }
    }
}
