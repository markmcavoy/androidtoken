package uk.co.bitethebullet.android.token;

import androidx.biometric.BiometricManager;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import java.util.Arrays;

import uk.co.bitethebullet.android.token.dialogs.PinDefintionDialog;

public class SettingsFragment extends PreferenceFragmentCompat {

    private FragmentActivity myContext;
    public static final int PIN_DEFINITION_DIALOG_FRAGMENT = 1;

    @Override
    public void onAttach(Activity activity) {
        myContext=(FragmentActivity) activity;
        super.onAttach(activity);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);

        final ListPreference lockPreferences = (ListPreference) findPreference("securityLock");
        final Fragment settingFragment = this;

        lockPreferences.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                setLockPreferencesData(lockPreferences);
                return false;
            }
        });

        lockPreferences.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference,
                                              Object newValue) {

                String oldValue = lockPreferences.getValue();

                Log.d("Preference", "IF show pin entry number");

                //if we have change to use a PIN code we need to get the
                //new pin value to secure the app
                if(newValue.toString().equals("1") && !oldValue.equals("1")){

                    Log.d("Preference", "show pin entry number");

                    DialogFragment newPinDefintion = new PinDefintionDialog();
                    newPinDefintion.setTargetFragment(settingFragment, PIN_DEFINITION_DIALOG_FRAGMENT);
                    newPinDefintion.show(myContext.getSupportFragmentManager(), "pinNewDefintion");
                }

                Log.d("Preference", "Old Value: " + oldValue + ", New Value: " + newValue.toString());
                return true;
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
        BiometricManager biometricManager = BiometricManager.from(this.getContext());
        return biometricManager.canAuthenticate() == BiometricManager.BIOMETRIC_SUCCESS;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case PIN_DEFINITION_DIALOG_FRAGMENT:

                if (resultCode == Activity.RESULT_OK) {
                    // After Ok code.
                    Log.d("settings", "PIN set");

                } else if (resultCode == Activity.RESULT_CANCELED){
                    // After Cancel code.
                    Log.d("settings", "PIN Cancel");
                    final ListPreference lockPreferences = (ListPreference) findPreference("securityLock");
                    lockPreferences.setValue("0");
                }

                break;
        }
    }

}