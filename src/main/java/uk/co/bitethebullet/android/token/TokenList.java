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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import uk.co.bitethebullet.android.token.parse.UrlParser;
import uk.co.bitethebullet.android.token.util.FontManager;
import uk.co.bitethebullet.android.token.util.SeedConvertor;
import uk.co.bitethebullet.android.token.zxing.IntentIntegrator;
import uk.co.bitethebullet.android.token.zxing.IntentResult;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import org.w3c.dom.Text;

/**
 * Main entry point into Android Token application
 * 
 * Generates OATH compliant HOTP or TOTP tokens which can be used
 * in place of a hardware token.
 * 
 * For more information about this project visit
 * http://code.google.com/p/androidtoken/
 */
public class TokenList extends AppCompatActivity {
	
	private static final int ACTIVITY_ADD_TOKEN = 0;
	private static final int ACTIVITY_CHANGE_PIN = 1;
	private static final int ACTIVITY_REMOVE_PIN = 2;

	private static final int MENU_SETTINGS = Menu.FIRST + 5;
	private static final int MENU_ADD_ID = Menu.FIRST;
	private static final int MENU_PIN_CHANGE_ID = Menu.FIRST + 1;
	private static final int MENU_PIN_REMOVE_ID = Menu.FIRST + 2;
	private static final int MENU_DELETE_TOKEN_ID = Menu.FIRST + 3;
	private static final int MENU_SCAN_QR = Menu.FIRST + 4;
	
	private static final int DIALOG_INVALID_PIN = 0;
	private static final int DIALOG_OTP = 1;
	private static final int DIALOG_DELETE_TOKEN = 2;
	
	private static final String KEY_HAS_PASSED_PIN = "pinValid";
	private static final String KEY_SELECTED_TOKEN_ID = "selectedTokenId";
		
	private Boolean mHasPassedPin = false;
	private Long mSelectedTokenId = Long.parseLong("-1");
	private Long mTokenToDeleteId = Long.parseLong("-1");
	private Timer mTimer = null;
	private TokenDbAdapter mTokenDbHelper = null;
	private Handler mHandler;
	private Runnable mOtpUpdateTask;
	
	private LinearLayout mMainPin;
	private LinearLayout mMainList;
	
	private TokenAdapter mtokenAdaptor = null;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        //check if we need to restore from a saveinstancestate
        if(savedInstanceState != null){
        	mHasPassedPin = savedInstanceState.getBoolean(KEY_HAS_PASSED_PIN);
        	mSelectedTokenId = savedInstanceState.getLong(KEY_SELECTED_TOKEN_ID);
        }
        
        mTokenDbHelper = new TokenDbAdapter(this);
        mTokenDbHelper.open();
        
        //if we have a pin defined, need to enter that first before allow
        //the user to see the tokens        
        mMainPin = (LinearLayout)findViewById(R.id.mainPin);
        mMainList = (LinearLayout)findViewById(R.id.mainList);
        
        Button loginBtn = (Button)findViewById(R.id.mainLogin);
        
        loginBtn.setOnClickListener(validatePin);
        
        if(PinManager.hasPinDefined(this) & !mHasPassedPin){
        	mMainPin.setVisibility(View.VISIBLE);
        	mMainList.setVisibility(View.GONE);
        }else{
        	mMainList.setVisibility(View.VISIBLE);
        	mMainPin.setVisibility(View.GONE);
        	mHasPassedPin = true;
        	fillData();

			ListView lv = (ListView)findViewById(android.R.id.list);
			TextView tvEmpty = (TextView)findViewById(android.R.id.empty);

			if(lv.getCount() > 0){
				tvEmpty.setVisibility(View.GONE);
			}else{
				tvEmpty.setVisibility(View.VISIBLE);
			}
        }
        
        mHandler = new Handler();
        
                
        ListView lv = (ListView)findViewById(android.R.id.list);
        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

			public boolean onItemLongClick(AdapterView<?> arg0, View v,
					int pos, long id) {
				return onLongListItemClick(v,pos,id);
			}
		});
    }
    

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mTokenDbHelper.close();
	}
	
	
	@Override
	protected void onResume() {
		super.onResume();
		
		Runnable otpUpdate = new Runnable(){

			public void run() {				
				if(mOtpUpdateTask == this){
					TokenList.this.fillData();
					
					mHandler.postDelayed(mOtpUpdateTask, 1000);
				}
			}			
		};
		
		mOtpUpdateTask = otpUpdate;
		mOtpUpdateTask.run();
	}


	@Override
	protected void onPause() {
		super.onPause();
		
		mOtpUpdateTask = null;
	}


	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean(KEY_HAS_PASSED_PIN, mHasPassedPin);
		outState.putLong(KEY_SELECTED_TOKEN_ID, mSelectedTokenId);
	}
	
	private OnClickListener validatePin = new 	OnClickListener() {
		
		public void onClick(View v) {
			
			String pin = ((EditText)findViewById(R.id.mainPinEdit)).getText().toString();
			
			if(PinManager.validatePin(v.getContext(), pin)){
				//then display the list view
				mMainList.setVisibility(View.VISIBLE);
				mMainPin.setVisibility(View.GONE);
				mHasPassedPin = true;
				fillData();
			}else{
				//display an alert
				showDialog(DIALOG_INVALID_PIN);
			}
		}
	};
	
	
	protected boolean onLongListItemClick(View v, int pos, final long id) {
	    Log.i("", "onLongListItemClick id=" + id);
	    
	    //prompt the user to see if they want to delete the current
	    //selected token
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		
		Cursor c = mTokenDbHelper.fetchAllTokens();
		startManagingCursor(c);
					
		builder.setTitle(R.string.app_name)
			   .setMessage(R.string.confirmDelete)
			   .setIcon(android.R.drawable.ic_dialog_alert)
			   .setPositiveButton(R.string.dialogPositive, new DialogInterface.OnClickListener() {				
					public void onClick(DialogInterface dialog, int which) {						
						mTokenDbHelper.deleteToken(id);
						
						Toast.makeText(getApplicationContext(), R.string.toastDeleted, Toast.LENGTH_SHORT).show();
						
						mtokenAdaptor = null;
						fillData();
					}
			   })
			   .setNegativeButton(R.string.dialogNegative, null);
		
		builder.show();
	    
	    
	    return true;
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


	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog d;
		
		switch(id){
		
		case DIALOG_INVALID_PIN:
			d = createAlertDialog(R.string.pinAlertInvalidPin);
			break;
			
		case DIALOG_OTP:
			d = new Dialog(this);

			d.setContentView(R.layout.otpdialog);
			d.setTitle(R.string.otpDialogTitle);

			ImageView image = (ImageView) d.findViewById(R.id.otpDialogImage);
			image.setImageResource(R.drawable.androidtoken);
			d.setOnDismissListener(dismissOtpDialog);
			break;
			
		case DIALOG_DELETE_TOKEN:			
			d = createDeleteTokenDialog();			
			break;
			
		default:
			d = null;
		
		}
		
		return d;
	}

	
	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		super.onPrepareDialog(id, dialog);
		
		switch(id){
		case DIALOG_OTP:
			
			TextView text = (TextView) dialog.findViewById(R.id.otpDialogText);
			
			
			//occurs if we rotate the screen while displaying a token
			if(mSelectedTokenId != -1)
			{
				text.setText(generateOtp(mSelectedTokenId));
			}
			
			mTimer = new Timer("otpCancel");
			mTimer.schedule(new CloseOtpDialog(this), 10 * 1000);			
			break;
			
		case DIALOG_DELETE_TOKEN:
			mTokenToDeleteId = Long.parseLong("-1");
			break;
		}
	}
	
	private class CloseOtpDialog extends TimerTask{

		private Activity mActivity;
		
		public CloseOtpDialog(Activity a){
			mActivity = a;
		}
		
		@Override
		public void run() {
			try
			{
				mActivity.dismissDialog(DIALOG_OTP);
			}
			catch(IllegalArgumentException ex){
				
			}
		}
		
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		
		if(mHasPassedPin){
			menu.findItem(MENU_PIN_REMOVE_ID).setEnabled(PinManager.hasPinDefined(this));
		}
		
		//if we have no tokens disable the delete token option
		ListView lv = (ListView)findViewById(android.R.id.list);
		menu.findItem(MENU_DELETE_TOKEN_ID).setEnabled(lv.getCount() > 0);
		
		return mHasPassedPin;
	}

	private void fillData() {
		
		if(mtokenAdaptor == null)
			mtokenAdaptor = new TokenAdapter(this, mTokenDbHelper);

		ListView lv = (ListView)findViewById(android.R.id.list);
		lv.setAdapter(mtokenAdaptor);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		Log.d("activityResult", "Activity Result received, request code:" + requestCode + " resultCode:" + resultCode);
		int toastRId = 0;
		
		IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
		if (scanResult != null) {
			
			if(storeOtpAuthUrl(scanResult.getContents())){
				mtokenAdaptor = null;
				fillData();				
				toastRId = R.string.toastAdded;
			}else{
				toastRId = R.string.toastInvalidQr;
			}
	  	}
		else if(requestCode == ACTIVITY_ADD_TOKEN && resultCode == Activity.RESULT_OK){
			mtokenAdaptor = null;
			fillData();
			toastRId = R.string.toastAdded;
		}
		
		if(toastRId > 0)
			Toast.makeText(getApplicationContext(), toastRId, Toast.LENGTH_SHORT).show();
	}

	private boolean storeOtpAuthUrl(String url) {
		try {
			
			Log.d("QR scanned URL", url);
			
			ITokenMeta token = UrlParser.parseOtpAuthUrl(url);
			
			String hexSeed = SeedConvertor.ConvertFromBA(
								SeedConvertor.ConvertFromEncodingToBA(token.getSecretBase32(),
																		SeedConvertor.BASE32_FORMAT),
								SeedConvertor.HEX_FORMAT);
			
			TokenDbAdapter db = new TokenDbAdapter(this.getBaseContext());
			db.open();
			long tokenId = db.createToken(token.getName(),
											"",
											hexSeed,
											token.getTokenType(),
											token.getDigits(),
											token.getTimeStep(),
											token.getOrganisation());
			
			//if we have created HOTP and counter is greater
			//than zero we need to set the token to this in the db
			if(token.getTokenType() == TokenMetaData.HOTP_TOKEN && token.getCounter() > 0){
				db.setTokenCounter(tokenId, token.getCounter());
			}
			
			db.close();
			
			return true;
			
		} catch (OtpAuthUriException e) {
			Log.e(TokenList.class.getName(), e.getMessage(), e);
			return false;
		}catch(IOException e){
			Log.e(TokenList.class.getName(), e.getMessage(), e);
			return false;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		menu.add(0, MENU_SETTINGS, 5, "Settings").setIcon(android.R.drawable.ic_menu_preferences);

		menu.add(0, MENU_ADD_ID, 0, R.string.menu_add_token).setIcon(android.R.drawable.ic_menu_add);	
		menu.add(0, MENU_PIN_CHANGE_ID, 1, R.string.menu_pin_change).setIcon(android.R.drawable.ic_lock_lock);
		menu.add(0, MENU_PIN_REMOVE_ID, 2, R.string.menu_pin_remove).setIcon(android.R.drawable.ic_menu_delete);
		menu.add(0, MENU_DELETE_TOKEN_ID, 3, R.string.menu_delete_token).setIcon(android.R.drawable.ic_menu_delete);
		menu.add(0, MENU_SCAN_QR, 4, R.string.menu_scan).setIcon(android.R.drawable.ic_menu_camera);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item) {

		switch(item.getItemId()){
			case MENU_ADD_ID:
				createToken();
				return true;

			case MENU_PIN_CHANGE_ID:
				changePin();
				return true;

			case MENU_PIN_REMOVE_ID:
				removePin();
				return true;

			case MENU_DELETE_TOKEN_ID:
				showDialog(DIALOG_DELETE_TOKEN);
				return true;

			case MENU_SCAN_QR:
				scanQR();
				return true;

			case MENU_SETTINGS:
				showSettings();
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void scanQR() {		
		IntentIntegrator integrator = new IntentIntegrator(this);
		integrator.initiateScan(IntentIntegrator.QR_CODE_TYPES);
	}


	private void removePin() {
		Intent i = new Intent(this, PinRemove.class);
		startActivityForResult(i, ACTIVITY_REMOVE_PIN);
	}

	private void changePin() {
		Intent i = new Intent(this,	 PinChange.class);
		startActivityForResult(i, ACTIVITY_CHANGE_PIN);
	}

	/**
	 * Starts the process of creating a new token in the application
	 */
	private void createToken() {
		Intent intent = new Intent(this, TokenAdd.class);
		startActivityForResult(intent, ACTIVITY_ADD_TOKEN);		
	}

	private void showSettings(){
		Intent intent = new Intent(this, SettingActivity.class);
		startActivity(intent);
	}

//	@Override
//	protected void onListItemClick(ListView l, View v, int position, long id) {
//		super.onListItemClick(l, v, position, id);
//
//		mSelectedTokenId = id;
//		showDialog(DIALOG_OTP);
//	}
	
	private DialogInterface.OnDismissListener dismissOtpDialog = new DialogInterface.OnDismissListener() {
		
		public void onDismiss(DialogInterface dialog) {
			if(mTimer != null){
				mTimer.cancel();
			}			
		}
	};


	private String generateOtp(long tokenId) {		
		Cursor cursor = mTokenDbHelper.fetchToken(tokenId);
		IToken token = TokenFactory.CreateToken(cursor);
		cursor.close();
		
		String otp = token.generateOtp();
		
		if(token instanceof HotpToken)
			mTokenDbHelper.incrementTokenCount(tokenId);		
		
		return otp;
	}
	
	private Dialog createDeleteTokenDialog() {
		Dialog d;
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		
		Cursor c = mTokenDbHelper.fetchAllTokens();
		startManagingCursor(c);
					
		builder.setTitle(R.string.app_name)
			   .setSingleChoiceItems(c, -1, TokenDbAdapter.KEY_TOKEN_NAME, deleteTokenEvent)
			   .setPositiveButton(R.string.dialogPositive, deleteTokenPositiveEvent)
			   .setNegativeButton(R.string.dialogNegative, deleteTokenNegativeEvent);
		
		d = builder.create();
		return d;
	}
	
	private DialogInterface.OnClickListener deleteTokenPositiveEvent = new DialogInterface.OnClickListener() {
		
		public void onClick(DialogInterface dialog, int which) {
			
			if(mTokenToDeleteId > 0){
				mTokenDbHelper.deleteToken(mTokenToDeleteId);
				mTokenToDeleteId = Long.parseLong("-1");
				mtokenAdaptor = null;
				fillData();
				removeDialog(DIALOG_DELETE_TOKEN);
			}
			
		}
	};
	
	private DialogInterface.OnClickListener deleteTokenNegativeEvent = new DialogInterface.OnClickListener() {
		
		public void onClick(DialogInterface dialog, int which) {
			removeDialog(DIALOG_DELETE_TOKEN);			
		}
	};
	
	private DialogInterface.OnClickListener deleteTokenEvent = new DialogInterface.OnClickListener() {
		
		public void onClick(DialogInterface dialog, int which) {
			Cursor c = mTokenDbHelper.fetchAllTokens();
			startManagingCursor(c);
			
			c.moveToPosition(which);
			mTokenToDeleteId = c.getLong(c.getColumnIndexOrThrow(TokenDbAdapter.KEY_TOKEN_ROWID));			
		}
	};
	
	private class TokenAdapter extends BaseAdapter
	{
		private Context mContext;
		private TokenDbAdapter mDbAdapter;
		private List<IToken> mTokens;
		private ColorStateList mDefaultTextView;
		
		public TokenAdapter(Context context, TokenDbAdapter dbAdapter){
			mContext = context;
			mDbAdapter = dbAdapter;

			Cursor cursor = mDbAdapter.fetchAllTokens();
			startManagingCursor(cursor);
			
			//read all the tokens we have and put them into a list
			//this will save hitting the db everytime we draw the
			//ui with an update
			
			mTokens = new ArrayList<IToken>();
			
			cursor.moveToFirst();
	        while (!cursor.isAfterLast()) {
	            mTokens.add(TokenFactory.CreateToken(cursor));
	            cursor.moveToNext();
	        }
			
		}
		
		public int getCount() {			
			return mTokens.size();
		}

		public Object getItem(int position) {
			return mTokens.get(position);
		}

		public long getItemId(int position) {
			return mTokens.get(position).getId();
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View row =  inflater.inflate(R.layout.token_list_row, null);

			TextView nameText = (TextView)row.findViewById(R.id.tokenrowtextname);
			TextView serialText = (TextView)row.findViewById(R.id.tokenrowtextserial);
			TextView totpText = (TextView)row.findViewById(R.id.tokenRowTimeTokenOtp);

			TextView faIcon = (TextView)row.findViewById(R.id.faIcon);
			faIcon.setTypeface(FontManager.getTypeface(mContext, FontManager.FONTAWESOME));


			ProgressBar totpProgressBar = (ProgressBar)row.findViewById(R.id.totpTimerProgressbar);
			
			
			IToken currentToken = (IToken)getItem(position);

			if(currentToken.getOrganisation() == null){
				nameText.setText(currentToken.getName());
			}else{
				nameText.setText(currentToken.getName() + " (" + currentToken.getOrganisation() + ")");
			}

			if(currentToken.getSerialNumber().length() > 0)
				serialText.setText(currentToken.getSerialNumber());
			else{
				TextView serialCaption = (TextView)row.findViewById(R.id.tokenrowtextserialcaption);
				serialCaption.setVisibility(View.GONE);
				serialText.setVisibility(View.GONE);
			}
			
			//if the token is a time token, just display the current
			//value for the token. Event tokens will still need to
			//be click to display the otp
			if(currentToken.getTokenType() == TokenDbAdapter.TOKEN_TYPE_TIME){
				faIcon.setText(R.string.farClock);
				totpText.setVisibility(View.VISIBLE);
				totpText.setText(currentToken.generateOtp());
				
				totpProgressBar.setVisibility(View.VISIBLE);
				
				Date dt = new Date();
				float curSec = (float)dt.getSeconds();
				int progress;
				
				if(currentToken.getTimeStep() == 30){
					
					if(curSec > 30)
						curSec = curSec - 30;
						
					progress = (int)(100 - ((curSec/30)*100));
				}else{
					progress = (int)(100 - ((curSec/60)*100));					
				}
				
				totpProgressBar.setProgress(progress);

				//when we get to the last 5 seconds, lets change
				//the token colour to red as a warning
				if(progress > 10){
					totpText.setTextColor(nameText.getTextColors());
				}else{
					totpText.setTextColor(Color.RED);
				}
			}
			else {
				faIcon.setText(R.string.farPlusSquare);
			}

			return row;
		}
	
	}
	
}