package ru.beykerykt.lineageos.powerswitcher.ui;

import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import ru.beykerykt.lineageos.powerswitcher.R;

public class AppPowerProfileSpinnerAdapter extends BaseAdapter {

    private final LayoutInflater mInflater;
    private final int mTextColor;
    private final int[] mProfiles = {
            R.string.dont_apply,
            R.string.power_save_profile_text,
            R.string.bias_power_save_profile_text,
            R.string.balanced_profile_text,
            R.string.bias_performance_profile_text,
            R.string.high_performance_profile_text
    };

    public AppPowerProfileSpinnerAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(android.R.attr.textColorSecondary,
                typedValue, true);
        mTextColor = context.getColor(typedValue.resourceId);
    }

    @Override
    public int getCount() {
        return mProfiles.length;
    }

    @Override
    public Object getItem(int position) {
        return mProfiles[position];
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView view;
        if (convertView != null) {
            view = (TextView) convertView;
        } else {
            view = (TextView) mInflater.inflate(android.R.layout.simple_spinner_dropdown_item,
                    parent, false);
        }
        view.setText(mProfiles[position]);
        view.setTextColor(mTextColor);
        view.setTextSize(14f);
        return view;
    }
}