package uk.co.bitethebullet.android.token;

import android.os.Bundle;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import java.util.Arrays;

public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);

        final ListPreference lockPreferences = (ListPreference) findPreference("securityLock");

        lockPreferences.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                setLockPreferencesData(lockPreferences);
                return false;
            }
        });
    }

    protected void setLockPreferencesData(ListPreference lp) {
        CharSequence[] entries = this.getContext()
                                        .getResources()
                                        .getStringArray(R.array.listSecurityPrefKeys);

        CharSequence[] entryValues = this.getContext()
                                            .getResources()
                                            .getStringArray(R.array.listSecurityPrefValues);

        //if the device doesn't support biometric security
        //then we'll just remove that option for the list
        if(!getDeviceSupportsBioMetrics()){

            entries = Arrays.copyOf(entries, 2);
            entryValues = Arrays.copyOf(entryValues, 2);
        }

        lp.setEntries(entries);
        lp.setEntryValues(entryValues);
    }

    protected boolean getDeviceSupportsBioMetrics(){
        //todo: MM complete me
        return false;
    }
}