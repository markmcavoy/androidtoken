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

import androidx.appcompat.app.AppCompatActivity;

public class PinChange extends AppCompatActivity {

	private static final int DIALOG_INVALID_EXISTING_PIN = 0;
	private static final int DIALOG_DIFF_NEW_PIN = 1;
	private static final int DIALOG_NO_NEW_PIN = 2;
	
	Boolean hasExistingPin = true;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pinchange);
		
		if(!PinManager.hasPinDefined(this)){
			hasExistingPin = false;
			EditText existPinEdit = (EditText)findViewById(R.id.pinChangeExistingPinEdit);
			existPinEdit.setEnabled(false);
		}
		
		Button submitBtn = (Button)findViewById(R.id.pinChangeSubmit);
		submitBtn.setOnClickListener(submitClick);		
	}
	
	private OnClickListener submitClick = new 	OnClickListener() {
		
		public void onClick(View v) {
			//validate the existing pin
			if(hasExistingPin){
				
				String existingPin = ((EditText)findViewById(R.id.pinChangeExistingPinEdit)).getText().toString();
				
				if(!PinManager.validatePin(v.getContext(), existingPin)){
					//the pin entered is not the one stored, show
					//warning and stop					
					showDialog(DIALOG_INVALID_EXISTING_PIN);
					return;
				}
			}
			
			//validate the two new pins match
			String newPin1 = ((EditText)findViewById(R.id.pinChangeNew1Edit)).getText().toString();
			String newPin2 = ((EditText)findViewById(R.id.pinChangeNew2Edit)).getText().toString();
			
			if(newPin1.length() == 0){
				showDialog(DIALOG_NO_NEW_PIN);
				return;
			}
			
			if(!newPin1.contentEquals(newPin2)){
				showDialog(DIALOG_DIFF_NEW_PIN);
				return;
			}
			
			//store
			PinManager.storePin(v.getContext(), newPin1);
			finish();
		}
	};
	
	private Dialog createAlertDialog(int messageId){
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(messageId)
			   .setCancelable(false)
			   .setPositiveButton(R.string.dialogPositive, dialogClose);
		
		return builder.create();
		
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog d;
		
		switch(id){
		case DIALOG_DIFF_NEW_PIN:
			d = createAlertDialog(R.string.pinAlertNewPinsDifferent);
			break;
			
		case DIALOG_INVALID_EXISTING_PIN:
			d = createAlertDialog(R.string.pinAlertInvalidPin);
			break;
			
		case DIALOG_NO_NEW_PIN:
			d = createAlertDialog(R.string.pinAlertNewPinBlank);
			break;
			
		default:
			d = null;
		}
		
		return d;
	}

	private DialogInterface.OnClickListener dialogClose = new 	DialogInterface.OnClickListener() {
		
		public void onClick(DialogInterface dialog, int which) {
			dialog.dismiss();
		}
	};

}
