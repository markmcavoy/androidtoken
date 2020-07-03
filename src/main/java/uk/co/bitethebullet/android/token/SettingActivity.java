package uk.co.bitethebullet.android.token;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.os.Bundle;

public class SettingActivity extends AppCompatActivity  {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragContainer, new SettingsFragment())
                .commit();

    }
}