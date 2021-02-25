/*
 * Copyright Mark McAvoy - www.bitethebullet.co.uk 2009 - 2020
 * 
 * This file is part of Android Token.
 *
 * Android Token is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Android Token is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Android Token.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package uk.co.bitethebullet.android.token;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class PinRemove extends Activity {

	private static final int DIALOG_INVALID_PIN = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pinremove);
		
		Button removePinBtn = (Button)findViewById(R.id.pinRemoveSubmit);
		removePinBtn.setOnClickListener(removeBtn);
	}
	
	private OnClickListener removeBtn = new OnClickListener() {
		
		public void onClick(View v) {
			//valid the pin
			
			String pin = ((EditText)findViewById(R.id.pinRemoveExistingPinEdit)).getText().toString();
			
			if(PinManager.validatePin(v.getContext(), pin)){
				//todo: MM fix me
				//PinManager.removePin(v.getContext());
				finish();
			}else{
				// the pin isn't the same as the one stored, do nothing
				showDialog(DIALOG_INVALID_PIN);
				return;
			}
			
		}
	};

	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog d;
		
		switch(id){
		
		case DIALOG_INVALID_PIN:
			d = createAlertDialog(R.string.pinAlertInvalidPin);
			break;
			
			default:
				d = null;
				break;
		
		}		
		
		return d;
	}
	
	private Dialog createAlertDialog(int messageId){
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(messageId)
			   .setCancelable(false)
			   .setPositiveButton(R.string.dialogPositive, dialogClose);
		
		return builder.create();
		
	}
	
	private DialogInterface.OnClickListener dialogClose = new 	DialogInterface.OnClickListener() {
		
		public void onClick(DialogInterface dialog, int which) {
			dialog.dismiss();
		}
	};

}
