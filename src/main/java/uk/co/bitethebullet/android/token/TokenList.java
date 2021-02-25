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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import uk.co.bitethebullet.android.token.adapters.TokenAdapter;
import uk.co.bitethebullet.android.token.datalayer.TokenDbAdapter;
import uk.co.bitethebullet.android.token.dialogs.DeleteTokenDialog;
import uk.co.bitethebullet.android.token.dialogs.DeleteTokenPickerDialog;
import uk.co.bitethebullet.android.token.parse.OtpAuthUriException;
import uk.co.bitethebullet.android.token.parse.UrlParser;
import uk.co.bitethebullet.android.token.tokens.HotpToken;
import uk.co.bitethebullet.android.token.tokens.IToken;
import uk.co.bitethebullet.android.token.tokens.ITokenMeta;
import uk.co.bitethebullet.android.token.tokens.TokenFactory;
import uk.co.bitethebullet.android.token.tokens.TokenHelper;
import uk.co.bitethebullet.android.token.tokens.TokenMetaData;
import uk.co.bitethebullet.android.token.tokens.TotpToken;
import uk.co.bitethebullet.android.token.util.FontManager;
import uk.co.bitethebullet.android.token.util.SeedConvertor;
import uk.co.bitethebullet.android.token.zxing.IntentIntegrator;
import uk.co.bitethebullet.android.token.zxing.IntentResult;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Color;
import android.media.session.MediaSession;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.preference.PreferenceManager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

/**
 * Main entry point into Android Token application
 * 
 * Generates OATH compliant HOTP or TOTP tokens which can be used
 * in place of a hardware token.
 * 
 * For more information about this project visit
 * http://code.google.com/p/androidtoken/
 */
public class TokenList extends AppCompatActivity
	implements DeleteTokenDialog.DeleteTokenDialogListener,
				DeleteTokenPickerDialog.DeleteTokenDialogListener{
	
	private static final int ACTIVITY_ADD_TOKEN = 0;
	private static final int ACTIVITY_CHANGE_PIN = 1;
	private static final int ACTIVITY_REMOVE_PIN = 2;
	private static final int ACTIVITY_ABOUT = 3;

	private static final int MENU_ADD_ID = Menu.FIRST;
	private static final int MENU_PIN_CHANGE_ID = Menu.FIRST + 1;
	private static final int MENU_PIN_REMOVE_ID = Menu.FIRST + 2;
	private static final int MENU_DELETE_TOKEN_ID = Menu.FIRST + 3;
	private static final int MENU_SCAN_QR = Menu.FIRST + 4;
	private static final int MENU_SETTINGS = Menu.FIRST + 5;
	private static final int MENU_ABOUT = Menu.FIRST + 6;

	private static final String KEY_HAS_PASSED_PIN = "pinValid";
	private static final String KEY_SELECTED_TOKEN_ID = "selectedTokenId";
		
	private Boolean mHasPassedPin = false;
	private Long mSelectedTokenId = Long.parseLong("-1");
	private Timer mTimer = null;
	private TokenDbAdapter mTokenDbHelper = null;
	private Handler mHandler;
	private Runnable mOtpUpdateTask;
	
	private LinearLayout mMainPin;
	private FrameLayout mMainList;
	
	private TokenAdapter mtokenAdaptor = null;
	SharedPreferences sharedPreferences;
	
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
		sharedPreferences =
				PreferenceManager.getDefaultSharedPreferences(this);

        mTokenDbHelper = new TokenDbAdapter(this);
        mTokenDbHelper.open();
        
        //if we have a pin defined, need to enter that first before allow
        //the user to see the tokens        
        mMainPin = (LinearLayout)findViewById(R.id.mainPin);
        mMainList = (FrameLayout)findViewById(R.id.list);
        
        Button loginBtn = (Button)findViewById(R.id.mainLogin);
        loginBtn.setOnClickListener(validatePin);
        
        if(PinManager.hasPinDefined(this)){
        	mMainPin.setVisibility(View.VISIBLE);
        	mMainList.setVisibility(View.GONE);
        }else{
        	mMainList.setVisibility(View.VISIBLE);
        	mMainPin.setVisibility(View.GONE);
        	mHasPassedPin = true;
			InitTokenList();
		}
        
        mHandler = new Handler();
    }

	private void InitTokenList() {
		fillData();

		ListView lv = (ListView)findViewById(R.id.listTokens);
		TextView tvEmpty = (TextView)findViewById(R.id.empty);

		if(lv.getCount() > 0){
			tvEmpty.setVisibility(View.GONE);
		}else{
			tvEmpty.setVisibility(View.VISIBLE);
		}

		lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int index, long id) {
				//get the token, we only handle the click
				//event if the token is a HOTP, in which
				//case we then display a dialog with the
				//token code
				String otp = generateOtp(id);

				if(otp != null){
					showHotpToken(otp);
				}
			}
		});

		registerForContextMenu(lv);

		FloatingActionButton fab = findViewById(R.id.fab);
		fab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				scanQR();
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
	
	private OnClickListener validatePin = new OnClickListener() {
		
		public void onClick(View v) {

			//close the keyboard, so we can show/see the snackbar
			//message if the pin is invalid
			InputMethodManager inputManager = (InputMethodManager)
					getSystemService(Context.INPUT_METHOD_SERVICE);

			if(getCurrentFocus() != null){
				inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
						InputMethodManager.HIDE_NOT_ALWAYS);
			}

			EditText mainPinEdit = (EditText)findViewById(R.id.mainPinEdit);
			String pin = mainPinEdit.getText().toString();
			
			if(PinManager.validatePin(v.getContext(), pin)){
				//then display the list view
				mMainList.setVisibility(View.VISIBLE);
				mMainPin.setVisibility(View.GONE);
				mHasPassedPin = true;
				InitTokenList();
			}else{
				//reset the input edittext
				mainPinEdit.setText("");

				//display an alert
				Snackbar.make(findViewById(R.id.mainPin), R.string.invalid_pin, Snackbar.LENGTH_LONG)
						.setAction("Action", null).show();
			}
		}
	};


	/**
	 * handles showing the HOTP token in a self closing dialog
	 * @param token
	 */
	private void showHotpToken(String token){
		final AlertDialog.Builder dialog = new AlertDialog.Builder(this)
													.setTitle(token);

		dialog.setPositiveButton(R.string.dismiss, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
			}
		});

		final AlertDialog alert = dialog.create();
		alert.show();

		// Hide after some seconds
		final Handler handler  = new Handler();
		final Runnable runnable = new Runnable() {
			@Override
			public void run() {
				if (alert.isShowing()) {
					alert.dismiss();
				}
			}
		};

		alert.setOnDismissListener(new DialogInterface.OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialog) {
				handler.removeCallbacks(runnable);
			}
		});
		handler.postDelayed(runnable, 8000);
	}




	/**
	Show the context menu's delete token dialog
	 **/
	public void showDeleteTokenDialog(IToken token) {
		DialogFragment dialog = new DeleteTokenDialog();

		Bundle args = new Bundle();
		args.putCharSequence("name", token.getFullName());
		args.putLong("tokenId", token.getId());

		dialog.setArguments(args);

		dialog.show(getSupportFragmentManager(), "NoticeDialogFragment");
	}

	@Override
	public void onDialogPositiveClick(DialogFragment dialog) {
		Long tokenId = dialog.getArguments().getLong("tokenId");
		mTokenDbHelper.deleteToken(tokenId);
		mtokenAdaptor = null;
		fillData();

		Snackbar.make(findViewById(R.id.list), R.string.token_is_deleted, Snackbar.LENGTH_LONG)
				.setAction("Action", null).show();
	}

	@Override
	public void onDialogNegativeClick(DialogFragment dialog) {

	}


	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {

		for (int i = 0; i < menu.size(); i++){
			menu.getItem(i).setEnabled(true);
		}

		//if we have no tokens disable the delete token option
		ListView lv = findViewById(R.id.listTokens);
		menu.findItem(MENU_DELETE_TOKEN_ID).setEnabled(lv.getCount() > 0);

		if(mHasPassedPin){
			//menu.findItem(MENU_PIN_REMOVE_ID).setEnabled(PinManager.hasPinDefined(this));
		}else{
			for (int i = 0; i < menu.size(); i++){
				menu.getItem(i).setEnabled(false);
			}
		}

		super.onPrepareOptionsMenu(menu);
		return true;
	}

	private void fillData() {
		ListView lv = (ListView)findViewById(R.id.listTokens);

		if(mtokenAdaptor == null){
			mtokenAdaptor = new TokenAdapter(this, mTokenDbHelper);
			lv.setAdapter(mtokenAdaptor);
		}else{
			mtokenAdaptor.notifyDataSetChanged();
		}

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
		
		if(toastRId > 0){
			Snackbar.make(findViewById(R.id.list), toastRId, Snackbar.LENGTH_LONG)
					.setAction("Action", null).show();
		}
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

		menu.add(0, MENU_ADD_ID, 0, R.string.menu_add_token).setIcon(android.R.drawable.ic_menu_add);	
		menu.add(0, MENU_PIN_CHANGE_ID, 1, R.string.menu_pin_change).setIcon(android.R.drawable.ic_lock_lock);
		//menu.add(0, MENU_PIN_REMOVE_ID, 2, R.string.menu_pin_remove).setIcon(android.R.drawable.ic_menu_delete);
		menu.add(0, MENU_DELETE_TOKEN_ID, 2, R.string.menu_delete_token).setIcon(android.R.drawable.ic_menu_delete);
		menu.add(0, MENU_SCAN_QR, 3, R.string.menu_scan).setIcon(android.R.drawable.ic_menu_camera);
		menu.add(0, MENU_SETTINGS, 4, R.string.menu_settings).setIcon(android.R.drawable.ic_menu_preferences);
		menu.add(0, MENU_ABOUT, 5, R.string.menu_about);

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
				showDeletePickerDialog();
				return true;

			case MENU_SCAN_QR:
				scanQR();
				return true;

			case MENU_SETTINGS:
				showSettings();
				return true;

			case MENU_ABOUT:
				showAbout();
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void showAbout(){
		Intent intent = new Intent(this, About.class);
		startActivityForResult(intent, ACTIVITY_ABOUT);
	}

	private void showDeletePickerDialog(){
		DialogFragment dialog = new DeleteTokenPickerDialog();

		CharSequence[] tokenNames = new TokenHelper(mTokenDbHelper)
											.getTokenFullNames();

		Bundle args = new Bundle();
		args.putCharSequenceArray("tokens", tokenNames);

		dialog.setArguments(args);
		dialog.show(getSupportFragmentManager(), "DeleteTokenPickerDialog");
	}

	@Override
	public void onDeleteTokensDialogPositiveClick(DialogFragment dialog, ArrayList selectedTokens) {
		TokenHelper tokenHelper = new TokenHelper(this.mTokenDbHelper);
		ArrayList tokens = tokenHelper.getTokens();
		ArrayList tokensToDelete =  new ArrayList();

		//workout the tokens we need to delete, then just remove them
		//one by one
		for(int i = 0; i < selectedTokens.size(); i++){
			tokensToDelete.add(tokens.get((int)selectedTokens.get(i)));
		}

		for(int i = 0; i < tokensToDelete.size(); i++){
			mTokenDbHelper.deleteToken(((IToken)tokensToDelete.get(i)).getId());
		}
		mtokenAdaptor = null;
		fillData();
	}

	@Override
	public void onDeleteTokensDialogNegativeClick(DialogFragment dialog) {
		//ignore nothing required
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


	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.token_item_menu, menu);

		Log.d(null, "onCreateContextMenu: ");
	}

	private IToken tokenAtPos(int position) {
		ListView lv = findViewById(R.id.listTokens);

		IToken token = (IToken) lv.getAdapter().getItem(position);
		return token;
	}

	@Override
	public boolean onContextItemSelected(@NonNull MenuItem item) {
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
		IToken token = tokenAtPos(info.position);

		boolean tokenClicked = token != null;

		if (tokenClicked) {

			switch (item.getItemId()) {

				//this menu option is hidden, so no need to complete
				case R.id.token_change_icon:
					Toast.makeText(this,"change icon",Toast.LENGTH_SHORT).show();
					return true;

				case R.id.token_delete:
					this.showDeleteTokenDialog(token);
					return true;

				case R.id.token_copy_secret:
					copyTokenSeedToClipboard(token);
					return true;

				case R.id.token_generate_qr_code:
					//generate the QR image and output
					//to a activity with the image/QR shown
					Intent intent = new Intent(this, QRCodeActivity.class);
					intent.putExtra("qrUrl", token.getUrl());
					intent.putExtra("fullName", token.getFullName());
					startActivity(intent);

					return true;

				default:
					return super.onContextItemSelected(item);
			}
		} else {
			return super.onContextItemSelected(item);
		}
	}

	private void copyTokenSeedToClipboard(IToken token) {
		ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
		clipboard.setPrimaryClip(ClipData.newPlainText( "Seed for " + token.getFullName(), token.getSeed()));


		Snackbar.make(findViewById(R.id.list), R.string.token_seed_copied, Snackbar.LENGTH_LONG)
				.setAction("Action", null).show();
	}


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
		

		if(!(token instanceof TotpToken)){
			String otp = TokenAdapter.otpFormatter(token.generateOtp(),
					sharedPreferences.getBoolean("groupIntoTwoDigits", false));
			mTokenDbHelper.incrementTokenCount(tokenId);
			return otp;
		}
		
		return null;
	}

}