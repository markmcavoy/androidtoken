package uk.co.bitethebullet.android.token.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;

import java.util.ArrayList;

import uk.co.bitethebullet.android.token.R;

public class PinDefintionDialog extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        //selectedItems = new ArrayList();

        //CharSequence[] tokenNames = this.getArguments().getCharSequenceArray("tokens");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.delete_token_dialog_title)
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
                .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

//                        listener.onDeleteTokensDialogPositiveClick(DeleteTokenPickerDialog.this,
//                                selectedItems);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
//                        listener.onDeleteTokensDialogNegativeClick(DeleteTokenPickerDialog.this);
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }
}
