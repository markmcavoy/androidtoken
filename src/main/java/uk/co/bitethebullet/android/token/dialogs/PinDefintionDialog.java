package uk.co.bitethebullet.android.token.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;

import java.util.ArrayList;

import uk.co.bitethebullet.android.token.R;

public class PinDefintionDialog extends DialogFragment {



    public interface PinDefinitionDialogListener {
        public void onDialogPositiveClick(DialogFragment dialog);
        public void onDialogNegativeClick(DialogFragment dialog);
    }

    PinDefintionDialog.PinDefinitionDialogListener listener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (PinDefintionDialog.PinDefinitionDialogListener)context;
        } catch (ClassCastException e) {
            throw new ClassCastException("Must implement DeleteTokenDialogListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(R.layout.pindefinitiondialog)
                .setTitle(R.string.set_pin)
//                .setMultiChoiceItems(tokenNames, null,
//                        new DialogInterface.OnMultiChoiceClickListener(){
//                            @Override
//                            public void onClick(DialogInterface dialogInterface, int i, boolean isChecked) {
//                                if (isChecked) {
//                                    selectedItems.add(i);
//                                } else if (selectedItems.contains(i)) {
//                                    selectedItems.remove(Integer.valueOf(i));
//                                }
//                            }
//                        })
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                       listener.onDialogPositiveClick(PinDefintionDialog.this);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                       listener.onDialogNegativeClick(PinDefintionDialog.this);
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }
}
