package uk.co.bitethebullet.android.token.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;

import uk.co.bitethebullet.android.token.R;

public class DeleteTokenDialog extends DialogFragment {

    public interface DeleteTokenDialogListener {
        public void onDialogPositiveClick(DialogFragment dialog);
        public void onDialogNegativeClick(DialogFragment dialog);
    }

    DeleteTokenDialogListener listener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (DeleteTokenDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException("Must implement DeleteTokenDialogListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        CharSequence tokenName = this.getArguments().getCharSequence("name");
        String message = getResources()
                            .getString(R.string.delete_token_dialog_message)
                            .replace("[name]", tokenName);


        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(message)
                .setTitle(R.string.delete_token_dialog_title)
                .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        listener.onDialogPositiveClick(DeleteTokenDialog.this);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        listener.onDialogNegativeClick(DeleteTokenDialog.this);
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }

}
