/*
 * Copyright Mark McAvoy - www.bitethebullet.co.uk 2009
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

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;

import uk.co.bitethebullet.android.token.util.*;

public class TokenAdd extends Activity {

	private static final int DIALOG_STEP1_NO_NAME = 0;
	private static final int DIALOG_STEP1_NO_SERIAL = 1;
	private static final int DIALOG_STEP2_NO_SEED = 2;
	private static final int DIALOG_STEP2_INVALID_SEED = 3;
	
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
	
	//current state of the activity
	private int mCurrentActivityStep = ACTIVITY_STEP_ONE;
	
	//holds the data from step 1
	private String mName;
	private String mSerial;
	private int mTokenType;
	private int mOtpLength;
	private int mTimeStep;
	private int mTokenSeedFormat;
	
	private static final int RANDOM_SEED_LENGTH = 160;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.token_add);
		
		loadSpinnerArrayData(R.id.tokenTypeSpinner, R.array.tokenType);
		loadSpinnerArrayData(R.id.tokenOtpSpinner, R.array.otpLength);
		loadSpinnerArrayData(R.id.tokenTimeStepSpinner, R.array.timeStep);
		loadSpinnerArrayData(R.id.tokenSeedFormat, R.array.tokenSeedFormatType);
		
		if(savedInstanceState != null){
			mCurrentActivityStep = savedInstanceState.getInt(KEY_ACTIVITY_STATE);
			
			int tokenType = savedInstanceState.getInt(KEY_TOKEN_TYPE);
			int otpLength = savedInstanceState.getInt(KEY_OTP_LENGTH);
			int timeStep = savedInstanceState.getInt(KEY_TIME_STEP);
			String tokenName = savedInstanceState.getString(KEY_NAME);
			String tokenSerial = savedInstanceState.getString(KEY_SERIAL);			
			
			if(mCurrentActivityStep == ACTIVITY_STEP_TWO){
				//step 2
				mName = tokenName;
				mSerial = tokenSerial;
				mTokenType = tokenType;
				mOtpLength = otpLength;
				mTimeStep = timeStep;				
				
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
				seed = SeedConvertor.ConvertFromBA(SeedConvertor.ConvertFromEncodingToBA(seed, mTokenSeedFormat), arg2);
			}
			catch(IOException ex){
				//todo: MM cancel the change of seed format, we have
				//some error
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
			
		case DIALOG_STEP2_INVALID_SEED:
			d = createAlertDialog(R.string.tokenAddDialogInvalidSeed);
			break;
			
		default:
			d = null;
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


	private OnClickListener buttonNext = new OnClickListener() {
		
		public void onClick(View v) {
			//validate we have the required completed fields
			
			boolean isValid = true;
			
			String name = ((EditText)findViewById(R.id.tokenNameEdit)).getText().toString();
			String serial = ((EditText)findViewById(R.id.tokenSerialEdit)).getText().toString();
			
			if(name.length() == 0){
				isValid = false;
				showDialog(DIALOG_STEP1_NO_NAME);				
			}
			
			if(isValid){
				//store step 1 values in members vars
				mName = name;
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
			} catch (IOException e) {
			}
			
			if(seed.length() == 0){
				isValid = false;
				showDialog(DIALOG_STEP2_NO_SEED);
				return;
			}
			
			if(!rbPassword.isChecked()){
			
				//validate the length
				int seedLength = seed.length();
				
				if(seedLength < 32){
					isValid = false;
					showDialog(DIALOG_STEP2_INVALID_SEED);
					return;
				}
				
				//valid the chars in the seed
				Pattern p = Pattern.compile("[A-Fa-f0-9]*");
				Matcher matcher = p.matcher(seed);
				
				if(!matcher.matches()){
					showDialog(DIALOG_STEP2_INVALID_SEED);
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
				db.createToken(mName, mSerial, seed, mTokenType, mOtpLength, mTimeStep);
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
				seedEdit.setText(HotpToken.generateNewSeed(RANDOM_SEED_LENGTH));
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
		Spinner spinner = (Spinner)findViewById(spinnerId);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, arrayData, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
	}


	private void showStepTwo() {
		//show the next step
		LinearLayout step1 = (LinearLayout)findViewById(R.id.tokenAddStep1);
		LinearLayout step2 = (LinearLayout)findViewById(R.id.tokenAddStep2);
		
		step1.setVisibility(View.GONE);
		step2.setVisibility(View.VISIBLE);
	}

}
