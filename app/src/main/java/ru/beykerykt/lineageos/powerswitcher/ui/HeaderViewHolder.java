package ru.beykerykt.lineageos.powerswitcher.ui;

import android.support.annotation.NonNull;
import android.support.v7.internal.widget.PreferenceImageView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import ru.beykerykt.lineageos.powerswitcher.R;

public class HeaderViewHolder extends RecyclerView.ViewHolder {

    public TextView summary;
    public PreferenceImageView icon;

    public HeaderViewHolder(@NonNull View itemView) {
        super(itemView);
        this.summary = (TextView) itemView.findViewById(R.id.header_summary);
        this.icon = (PreferenceImageView) itemView.findViewById(R.id.header_icon);
    }
}