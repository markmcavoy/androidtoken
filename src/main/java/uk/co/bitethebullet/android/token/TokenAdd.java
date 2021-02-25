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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;

import androidx.appcompat.app.AppCompatActivity;

import uk.co.bitethebullet.android.token.datalayer.TokenDbAdapter;
import uk.co.bitethebullet.android.token.tokens.HotpToken;
import uk.co.bitethebullet.android.token.util.*;

public class TokenAdd extends AppCompatActivity {

	private static final int DIALOG_STEP1_NO_NAME = 0;
	private static final int DIALOG_STEP1_NO_SERIAL = 1;
	private static final int DIALOG_STEP2_NO_SEED = 2;
	private static final int DIALOG_STEP2_INVALID_SEED_TOO_SHORT = 3;
	private static final int DIALOG_STEP2_INVALID_SEED_NOT_HEX = 4;
	private static final int DIALOG_STEP2_SEED_CONVERT_ERROR = 5;
	private static final int DIALOG_STEP2_UNABLE_TO_SWITCH_FORMAT = 6;

	//defines the define steps the activity can display
	private static final int ACTIVITY_STEP_ONE = 0;
	private static final int ACTIVITY_STEP_TWO = 1;
	
	private static final String KEY_ACTIVITY_STATE = "currentState";
	private static final String KEY_TOKEN_TYPE = "tokenType";
	private static final String KEY_OTP_LENGTH = "otpLength";
	private static final String KEY_TIME_STEP = "timeStep";
	private static final String KEY_NAME = "tokenName";
	private static final String KEY_SERIAL = "tokenSerial";
	private static final String KEY_SEED_FORMAT = "tokenSeedFormat";
	private static final String KEY_ORGANISATION = "tokenOrganisation";
	
	//current state of the activity
	private int mCurrentActivityStep = ACTIVITY_STEP_ONE;
	
	//holds the data from step 1
	private String mName;
	private String mOrganisation;
	private String mSerial;
	private int mTokenType;
	private int mOtpLength;
	private int mTimeStep;
	private int mTokenSeedFormat;
	private Boolean mAcceptWeakSeed;
	
	private static final int RANDOM_SEED_LENGTH = 160;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mAcceptWeakSeed = false;
		
		setContentView(R.layout.token_add);
		
		loadSpinnerArrayData(R.id.tokenTypeSpinner, R.array.tokenType, 1);
		loadSpinnerArrayData(R.id.tokenOtpSpinner, R.array.otpLength);
		loadSpinnerArrayData(R.id.tokenTimeStepSpinner, R.array.timeStep);
		loadSpinnerArrayData(R.id.tokenSeedFormat, R.array.tokenSeedFormatType, 1);
		
		if(savedInstanceState != null){
			mCurrentActivityStep = savedInstanceState.getInt(KEY_ACTIVITY_STATE);
			
			int tokenType = savedInstanceState.getInt(KEY_TOKEN_TYPE);
			int otpLength = savedInstanceState.getInt(KEY_OTP_LENGTH);
			int timeStep = savedInstanceState.getInt(KEY_TIME_STEP);
			String tokenName = savedInstanceState.getString(KEY_NAME);
			String tokenSerial = savedInstanceState.getString(KEY_SERIAL);
			String tokenOrganisation = savedInstanceState.getString(KEY_ORGANISATION);
			
			if(mCurrentActivityStep == ACTIVITY_STEP_TWO){
				//step 2
				mName = tokenName;
				mSerial = tokenSerial;
				mTokenType = tokenType;
				mOtpLength = otpLength;
				mTimeStep = timeStep;
				mOrganisation = tokenOrganisation;
				
				showStepTwo();
				
			}else{
				//step 1
				((Spinner)findViewById(R.id.tokenTypeSpinner)).setSelection(tokenType);
				((Spinner)findViewById(R.id.tokenOtpSpinner)).setSelection(otpLength);
				((Spinner)findViewById(R.id.tokenTimeStepSpinner)).setSelection(timeStep);
			}
		}
		
		Button btnNext = (Button)findViewById(R.id.btnAddStep2);
		btnNext.setOnClickListener(buttonNext);
		
		RadioButton rbManual = (RadioButton)findViewById(R.id.rbSeedManual);
		RadioButton rbRandom = (RadioButton)findViewById(R.id.rbSeedRandom);
		RadioButton rbPassword = (RadioButton)findViewById(R.id.rbSeedPassword);
		
		rbManual.setOnClickListener(radioSeed);
		rbRandom.setOnClickListener(radioSeed);
		rbPassword.setOnClickListener(radioSeed);
		
		Button btnComplete = (Button)findViewById(R.id.tokenAddComplete);
		btnComplete.setOnClickListener(buttonComplete);	
		
		Spinner tokenType = (Spinner)findViewById(R.id.tokenTypeSpinner);
		tokenType.setOnItemSelectedListener(tokenTypeSelected);
		
		Spinner tokenSeedFormat = (Spinner)findViewById(R.id.tokenSeedFormat);
		tokenSeedFormat.setOnItemSelectedListener(tokenSeedFormatSelected);
	}
	
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
		int tokenType;
		int otpLength;
		int timeStep;
		
		if(mCurrentActivityStep == ACTIVITY_STEP_ONE){
			tokenType = ((Spinner)findViewById(R.id.tokenTypeSpinner)).getSelectedItemPosition();
			otpLength = ((Spinner)findViewById(R.id.tokenOtpSpinner)).getSelectedItemPosition();
			timeStep = ((Spinner)findViewById(R.id.tokenTimeStepSpinner)).getSelectedItemPosition();
			
			outState.putInt(KEY_TOKEN_TYPE, tokenType);
			outState.putInt(KEY_OTP_LENGTH, otpLength);
			outState.putInt(KEY_TIME_STEP, timeStep);
		}else{
			outState.putInt(KEY_TOKEN_TYPE, mTokenType);
			outState.putInt(KEY_OTP_LENGTH, mOtpLength);
			outState.putInt(KEY_TIME_STEP, mTimeStep);
		}
		
		outState.putInt(KEY_ACTIVITY_STATE, mCurrentActivityStep);
		outState.putString(KEY_NAME, mName);
		outState.putString(KEY_SERIAL, mSerial);
		outState.putString(KEY_ORGANISATION, mOrganisation);
	}


	private OnItemSelectedListener tokenTypeSelected = new OnItemSelectedListener() {

		public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {

			TextView caption = (TextView)findViewById(R.id.tokenTimeStep);
			Spinner spinner = (Spinner)findViewById(R.id.tokenTimeStepSpinner);
			
			if(arg2 == 0){
				caption.setVisibility(View.INVISIBLE);
				spinner.setVisibility(View.INVISIBLE);
			}else{
				caption.setVisibility(View.VISIBLE);
				spinner.setVisibility(View.VISIBLE);
			}
			
		}

		public void onNothingSelected(AdapterView<?> arg0) {
			//ignore			
		}
		
	};
	
	
	private OnItemSelectedListener tokenSeedFormatSelected = new OnItemSelectedListener() {

		public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			
			EditText tokenSeedEdit = (EditText)findViewById(R.id.tokenSeedEdit);
			String seed = tokenSeedEdit.getText().toString();
			
			try{
				//only convert if we have something entered
				if(seed.length() > 0) {
					seed = SeedConvertor.ConvertFromBA(SeedConvertor.ConvertFromEncodingToBA(seed, mTokenSeedFormat), arg2);
				}
			}
			catch(Exception ex){
				//cancel the change of seed format, if have
				//some error
				showDialog(DIALOG_STEP2_UNABLE_TO_SWITCH_FORMAT);
				
				Spinner tokenSeedFormat = (Spinner)findViewById(R.id.tokenSeedFormat);
				tokenSeedFormat.setSelection(mTokenSeedFormat);
				return;
			}
			
			tokenSeedEdit.setText(seed);
			mTokenSeedFormat = arg2;			
		}

		public void onNothingSelected(AdapterView<?> arg0) {
			// ignore not required			
		}
		
	};
	

	
	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog d;
		
		switch(id){
		case DIALOG_STEP1_NO_NAME:
			d = createAlertDialog(R.string.tokenAddDialogNoName);
			break;
			
		case DIALOG_STEP1_NO_SERIAL:
			d = createAlertDialog(R.string.tokenAddDialogNoSerial);
			break;
			
		case DIALOG_STEP2_NO_SEED:
			d = createAlertDialog(R.string.tokenAddDialogNoSeed);
			break;
			
		case DIALOG_STEP2_INVALID_SEED_TOO_SHORT:
			d = createAlertDialog(R.string.tokenAddDialogInvalidSeedTooShort2);
			break;
			
		case DIALOG_STEP2_INVALID_SEED_NOT_HEX:
			d = createAlertDialog(R.string.tokenAddDialogInvalidSeedNotHex);
			break;
			
		case DIALOG_STEP2_SEED_CONVERT_ERROR:
			d = createAlertDialog(R.string.tokenAddDialogInvalidSeedConvertion);
			break;
			
		case DIALOG_STEP2_UNABLE_TO_SWITCH_FORMAT:
			d = createAlertDialog(R.string.tokenAddDialogUnableToSwitchFormat);
			break;
			
		default:
			d = null;
		}		
		
		return d;
	}
	
	private Dialog createAlertDialog(int messageId){
		return this.createAlertDialog(messageId, null, dialogClose, null);
	}
	
	private Dialog createAlertDialog(int messageId, 
									String additionalMessage, 
									DialogInterface.OnClickListener positiveClick,
									DialogInterface.OnClickListener negativeClick){
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(messageId)
			   .setPositiveButton(R.string.dialogPositive, positiveClick);
		
		if(negativeClick != null){
			builder.setNegativeButton(R.string.cancel, negativeClick);
		}else{
			builder.setCancelable(false);
		}
		
		//load the resource and optional additional information
		if(additionalMessage != null){
			builder.setMessage(String.format(getResources().getString(messageId), additionalMessage));
		}
		
		return builder.create();		
	}
	
	private DialogInterface.OnClickListener dialogClose = new DialogInterface.OnClickListener() {		
		public void onClick(DialogInterface dialog, int which) {
			dialog.dismiss();
		}
	};

	/***
	 * handles the user accepting the weak seed when we warn then in a dialod
	 */
	private DialogInterface.OnClickListener dialogAcceptWeakSeed = new DialogInterface.OnClickListener() {
		
		public void onClick(DialogInterface dialog, int which) {
			Log.d("Android Token", "dialogAcceptWeakSeed called");
			mAcceptWeakSeed = true;
			dialog.dismiss();	
			
			buttonComplete.onClick(getCurrentFocus());
			mAcceptWeakSeed = false;
		}
	};

	private OnClickListener buttonNext = new OnClickListener() {
		
		public void onClick(View v) {
			//validate we have the required completed fields
			
			boolean isValid = true;
			
			String name = ((EditText)findViewById(R.id.tokenNameEdit)).getText().toString();
			String organisation = ((EditText)findViewById(R.id.tokenOrganisationEdit)).getText().toString();
			String serial = ((EditText)findViewById(R.id.tokenSerialEdit)).getText().toString();
			
			if(name.length() == 0){
				isValid = false;
				showDialog(DIALOG_STEP1_NO_NAME);				
			}
			
			if(isValid){
				//store step 1 values in members vars
				mName = name;
				mOrganisation = organisation;
				mSerial = serial;
				mTokenType = ((Spinner)findViewById(R.id.tokenTypeSpinner)).getSelectedItemPosition();;
				mOtpLength = Integer.parseInt(((Spinner)findViewById(R.id.tokenOtpSpinner)).getSelectedItem().toString());
				mTimeStep = ((Spinner)findViewById(R.id.tokenTimeStepSpinner)).getSelectedItemPosition() == 0 ? 30 : 60;
				
				showStepTwo();				
				mCurrentActivityStep = ACTIVITY_STEP_TWO;
			}
		}
	};
	
	private OnClickListener buttonComplete = new OnClickListener() {
		
		public void onClick(View v) {
			//validate we have a valid serial
			Boolean isValid = true;
			
			RadioButton rbPassword = (RadioButton)findViewById(R.id.rbSeedPassword);
			String seed = ((EditText)findViewById(R.id.tokenSeedEdit)).getText().toString();
			
			try {
				//if the seed is not in hex format convert this to hex
				//then make sure the length is 
				if(mTokenSeedFormat == 1){
					//base32
					seed = SeedConvertor.ConvertFromBA(SeedConvertor.ConvertFromEncodingToBA(seed, 1), 0);
				}else if(mTokenSeedFormat == 2){
					//base 64
					seed = SeedConvertor.ConvertFromBA(SeedConvertor.ConvertFromEncodingToBA(seed, 2), 0);
				}
			} catch (Exception e) {
				showDialog(DIALOG_STEP2_SEED_CONVERT_ERROR);
				return;
			}
			
			if(seed.length() == 0){
				isValid = false;
				showDialog(DIALOG_STEP2_NO_SEED);
				return;
			}
			
			if(!rbPassword.isChecked()){
			
				//validate the length
				int seedLength = seed.length();
				
				//valid the chars in the seed
				Pattern p = Pattern.compile("[A-Fa-f0-9]*");
				Matcher matcher = p.matcher(seed);
				
				if(!matcher.matches()){
					showDialog(DIALOG_STEP2_INVALID_SEED_NOT_HEX);
					return;
				}
				
				if(seedLength < 2){
					showDialog(DIALOG_STEP2_INVALID_SEED_TOO_SHORT);
					return;
				}				
				else if(seedLength < 32 && !mAcceptWeakSeed){
					isValid = false;
					
					//the seed is shorter than the 128bit minimum as recommended
					//in the RFC, therefore warn the user but give them then
					//ability to create a weak seed anyway
					
					Dialog d = createAlertDialog(R.string.tokenAddDialogInvalidSeedTooShort, 
												 "" + seedLength * 4, 
												 dialogAcceptWeakSeed, 
												 dialogClose);
					d.show();
					
					return;
				}
				
			}else{
				//when creating a seed from password we simple sha1 the data then
				//use that as a seed to to concat with the data again
				//
				//  h1 = sha1(password)
				//  h2 = sha1(password + h1)
				//
				//h2 should then be stored as a hex string in the database
				try{
					
					byte[] input = seed.getBytes();
					MessageDigest md = MessageDigest.getInstance("SHA1");
					
					md.reset();
					byte[] h1 = md.digest(input);
					md.reset();
					byte[] h2 = md.digest(mergeByteArray(input, h1));
					
					seed = HotpToken.byteArrayToHexString(h2);
					
				}catch(NoSuchAlgorithmException nsae){
					
				}
			
			}
			
			if(isValid){
				//store token in db
				TokenDbAdapter db = new TokenDbAdapter(v.getContext());
				db.open();
				db.createToken(mName, mSerial, seed,
								mTokenType, mOtpLength,
								mTimeStep, mOrganisation);
				db.close();
				
				setResult(RESULT_OK);
				
				finish();
			}
			
		}
	};
	
	private byte[] mergeByteArray(byte[] b1, byte[] b2){
		
		byte[] result = new byte[b1.length + b2.length];
		
		int i = 0;
		
		for(byte b : b1){
			result[i] = b;
			i++;
		}
		
		for(byte b : b2){
			result[i] = b;
			i++;	
		}
		
		return result;		
	}
	
	private OnClickListener radioSeed = new OnClickListener() {
		
		public void onClick(View v) {
			RadioButton rb = (RadioButton)v;
			
			//if we are entering the seed manually/randomly we should display the
			//the format option to allow the user to enter as hex/base64/base32
			
			if(rb.getId() == R.id.rbSeedRandom){
				EditText seedEdit = (EditText)findViewById(R.id.tokenSeedEdit);

				try{
					//create a new random seed, this will be in hex format.
					//see if we need to convert this to whatever we have input
					//format selected as
					String hexSeed = HotpToken.generateNewSeed(RANDOM_SEED_LENGTH);
					byte[] ba = SeedConvertor.ConvertFromEncodingToBA(hexSeed,
																		SeedConvertor.HEX_FORMAT);

					seedEdit.setText(SeedConvertor.ConvertFromBA(ba, mTokenSeedFormat));
				}catch(Exception ex){
				}

			}
			
			Spinner tokenSeedFormat = (Spinner)findViewById(R.id.tokenSeedFormat);
			
			if(rb.getId() == R.id.rbSeedManual || rb.getId() == R.id.rbSeedRandom){
				tokenSeedFormat.setEnabled(true);
			}else{
				tokenSeedFormat.setEnabled(false);
			}
		}
	};

	private void loadSpinnerArrayData(int spinnerId, int arrayData){
		loadSpinnerArrayData(spinnerId, arrayData, -1);
	}

	private void loadSpinnerArrayData(int spinnerId, int arrayData, int selectedPosition){
		Spinner spinner = (Spinner)findViewById(spinnerId);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
																				arrayData,
																				android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);

		if(selectedPosition >= 0) {
			spinner.setSelection(selectedPosition);
		}
	}


	private void showStepTwo() {
		//show the next step
		RelativeLayout step1 = (RelativeLayout)findViewById(R.id.tokenAddStep1);
		RelativeLayout step2 = (RelativeLayout)findViewById(R.id.tokenAddStep2);
		
		step1.setVisibility(View.GONE);
		step2.setVisibility(View.VISIBLE);
	}

}
