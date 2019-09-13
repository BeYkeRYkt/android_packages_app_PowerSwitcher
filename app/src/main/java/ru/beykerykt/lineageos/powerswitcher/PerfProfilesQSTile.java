package ru.beykerykt.lineageos.powerswitcher;

import android.app.AlertDialog;
import android.app.Dialog;
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

public final class PerfProfilesQSTile extends TileService {
    private PerformanceManager mPerf;
    private int mCurrentProfile = -1;
    private static PerfProfilesQSTile mListeningInstance;

    @Override
    public void onCreate() {
        mPerf = PerformanceManager.getInstance(getApplicationContext());
    }

    @Override
    public void onStartListening() {
        mListeningInstance = this;
        mListeningInstance.updateQsTile();
    }

    @Override
    public void onStopListening() {
        if (mListeningInstance == this) {
            mListeningInstance = null;
        }
    }

    @Override
    public void onClick() {
        showDialog(createDialog());
    }

    public static void updateTile() {
        if (mListeningInstance != null) {
            mListeningInstance.updateQsTile();
        }
    }

    private final void updateQsTile() {
        Tile tile = getQsTile();
        int state = Tile.STATE_UNAVAILABLE;

        if (mPerf != null && mPerf.getNumberOfProfiles() > 0) {
            if (mPerf.getActivePowerProfile() != null) {
                state = Tile.STATE_ACTIVE;
                mCurrentProfile = mPerf.getActivePowerProfile().getId();
            } else {
                mCurrentProfile = -1;
            }
        } else {
            mCurrentProfile = -1;
        }

        switch (mCurrentProfile) {
            case PROFILE_POWER_SAVE:
                tile.setIcon(Icon.createWithResource(getApplicationContext(), R.drawable.ic_profile_power_save));
                tile.setLabel(getString(R.string.power_save_profile_text));
                break;
            case PROFILE_BALANCED:
                tile.setIcon(Icon.createWithResource(getApplicationContext(), R.drawable.ic_profile_balanced));
                tile.setLabel(getString(R.string.balanced_profile_text));
                break;
            case PROFILE_HIGH_PERFORMANCE:
                tile.setIcon(Icon.createWithResource(getApplicationContext(), R.drawable.ic_profile_high_performance));
                tile.setLabel(getString(R.string.high_performance_profile_text));
                break;
            case PROFILE_BIAS_POWER_SAVE:
                tile.setIcon(Icon.createWithResource(getApplicationContext(), R.drawable.ic_profile_bias_power_save));
                tile.setLabel(getString(R.string.bias_power_save_profile_text));
                break;
            case PROFILE_BIAS_PERFORMANCE:
                tile.setIcon(Icon.createWithResource(getApplicationContext(), R.drawable.ic_profile_bias_performance));
                tile.setLabel(getString(R.string.bias_performance_profile_text));
                break;
            default:
                tile.setIcon(Icon.createWithResource(getApplicationContext(), R.drawable.ic_profile_balanced));
                tile.setLabel(getString(R.string.profiles_tile_label));
                break;
        }
        tile.setState(state);
        tile.updateTile();
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
        AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext());
        builder.setTitle(R.string.profiles_tile_label)
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
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