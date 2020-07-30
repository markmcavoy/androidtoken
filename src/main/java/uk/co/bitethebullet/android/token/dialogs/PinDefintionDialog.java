package uk.co.bitethebullet.android.token.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.fragment.app.DialogFragment;
import androidx.preference.PreferenceManager;

import java.util.ArrayList;

import uk.co.bitethebullet.android.token.PinManager;
import uk.co.bitethebullet.android.token.R;

public class PinDefintionDialog extends DialogFragment {


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final Context myContext = this.getContext();
        View viewInflated = LayoutInflater.from(getContext())
                                            .inflate(R.layout.pindefinitiondialog,
                                                        (ViewGroup) getView(),
                                                    false);
        final EditText pinInput = (EditText) viewInflated.findViewById(R.id.pin);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setView(viewInflated)
                .setTitle(R.string.set_pin)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        PinManager.storePin(myContext, pinInput.getText().toString());

                        getTargetFragment()
                                    .onActivityResult(getTargetRequestCode(),
                                                        Activity.RESULT_OK,
                                                        getActivity().getIntent());
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        getTargetFragment()
                                .onActivityResult(getTargetRequestCode(),
                                        Activity.RESULT_CANCELED,
                                        getActivity().getIntent());
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }
}
