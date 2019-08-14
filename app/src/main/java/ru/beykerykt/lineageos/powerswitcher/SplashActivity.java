package ru.beykerykt.lineageos.powerswitcher;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/*
 * Use only for SplashScreen
 */
public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = new Intent(this, AppPerfProfilesActivity.class);
        startActivity(intent);
        finish();
    }
}
