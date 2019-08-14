package ru.beykerykt.lineageos.powerswitcher.ui;

import android.support.annotation.NonNull;
import android.support.v7.internal.widget.PreferenceImageView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Spinner;
import android.widget.TextView;

import ru.beykerykt.lineageos.powerswitcher.R;

public class AppViewHolder extends RecyclerView.ViewHolder {

    public TextView title;
    public Spinner mode;
    public PreferenceImageView icon;
    public View rootView;

    public AppViewHolder(@NonNull View itemView) {
        super(itemView);
        this.title = (TextView) itemView.findViewById(R.id.app_name);
        this.mode = (Spinner) itemView.findViewById(R.id.app_perf_profile);
        this.icon = (PreferenceImageView) itemView.findViewById(R.id.app_icon);
        this.rootView = itemView;
    }
}
