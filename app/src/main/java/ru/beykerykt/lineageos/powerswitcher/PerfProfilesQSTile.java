package ru.beykerykt.lineageos.powerswitcher;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.graphics.drawable.Icon;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

import lineageos.power.PerformanceManager;

import static lineageos.power.PerformanceManager.PROFILE_BALANCED;
import static lineageos.power.PerformanceManager.PROFILE_BIAS_PERFORMANCE;
import static lineageos.power.PerformanceManager.PROFILE_BIAS_POWER_SAVE;
import static lineageos.power.PerformanceManager.PROFILE_HIGH_PERFORMANCE;
import static lineageos.power.PerformanceManager.PROFILE_POWER_SAVE;

public class PerfProfilesQSTile extends TileService {
    private PerformanceManager mPerf;
    private int mCurrentProfile = -1;

    @Override
    public void onCreate() {
        mPerf = PerformanceManager.getInstance(this);
        TileService.requestListeningState(this, new ComponentName(this, PerfProfilesQSTile.class));
    }

    @Override
    public void onTileAdded() {
        updateQsTile();
    }

    @Override
    public void onStartListening() {
        if (mPerf.getActivePowerProfile() != null) {
            mCurrentProfile = mPerf.getActivePowerProfile().getId();
        }
        updateQsTile();
    }

    @Override
    public void onStopListening() {
        updateQsTile();
    }

    @Override
    public void onClick() {
        showDialog(createDialog());
    }

    private void updateQsTile() {
        if (mPerf != null && mPerf.getNumberOfProfiles() > 0) {
            getQsTile().setState(Tile.STATE_ACTIVE);
        } else {
            getQsTile().setState(Tile.STATE_UNAVAILABLE);
        }

        switch (mCurrentProfile) {
            case PROFILE_POWER_SAVE:
                getQsTile().setIcon(Icon.createWithResource(this, R.drawable.ic_profile_power_save));
                getQsTile().setLabel(getString(R.string.power_save_profile_text));
                break;
            case PROFILE_BALANCED:
                getQsTile().setIcon(Icon.createWithResource(this, R.drawable.ic_profile_balanced));
                getQsTile().setLabel(getString(R.string.balanced_profile_text));
                break;
            case PROFILE_HIGH_PERFORMANCE:
                getQsTile().setIcon(Icon.createWithResource(this, R.drawable.ic_profile_high_performance));
                getQsTile().setLabel(getString(R.string.high_performance_profile_text));
                break;
            case PROFILE_BIAS_POWER_SAVE:
                getQsTile().setIcon(Icon.createWithResource(this, R.drawable.ic_profile_bias_power_save));
                getQsTile().setLabel(getString(R.string.bias_power_save_profile_text));
                break;
            case PROFILE_BIAS_PERFORMANCE:
                getQsTile().setIcon(Icon.createWithResource(this, R.drawable.ic_profile_bias_performance));
                getQsTile().setLabel(getString(R.string.bias_performance_profile_text));
                break;
            default:
                getQsTile().setIcon(Icon.createWithResource(this, R.drawable.ic_profile_balanced));
                getQsTile().setLabel(getString(R.string.profiles_tile_label));
                break;
        }
        getQsTile().updateTile();
    }

    private int convertProfileToDialogItem() {
        int checkedItem = -1;
        switch (mCurrentProfile) {
            case PROFILE_POWER_SAVE:
                checkedItem = 0;
                break;
            case PROFILE_BALANCED:
                checkedItem = 2;
                break;
            case PROFILE_HIGH_PERFORMANCE:
                checkedItem = 4;
                break;
            case PROFILE_BIAS_POWER_SAVE:
                checkedItem = 1;
                break;
            case PROFILE_BIAS_PERFORMANCE:
                checkedItem = 3;
                break;
        }
        return checkedItem;
    }

    private Dialog createDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.profiles_tile_label)
                .setSingleChoiceItems(R.array.power_profiles_entries, convertProfileToDialogItem(),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case 0: // PROFILE_POWER_SAVE
                                        mCurrentProfile = PROFILE_POWER_SAVE;
                                        break;
                                    case 1: // PROFILE_BIAS_POWER_SAVE
                                        mCurrentProfile = PROFILE_BIAS_POWER_SAVE;
                                        break;
                                    case 2: // PROFILE_BALANCED
                                        mCurrentProfile = PROFILE_BALANCED;
                                        break;
                                    case 3: // PROFILE_BIAS_PERFORMANCE
                                        mCurrentProfile = PROFILE_BIAS_PERFORMANCE;
                                        break;
                                    case 4: // PROFILE_HIGH_PERFORMANCE
                                        mCurrentProfile = PROFILE_HIGH_PERFORMANCE;
                                        break;
                                }
                                if (mCurrentProfile >= 0) {
                                    mPerf.setPowerProfile(mCurrentProfile);
                                }
                                updateQsTile();
                                dialog.dismiss();
                            }
                        });
        return builder.create();
    }
}