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

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;

import androidx.preference.PreferenceManager;

import uk.co.bitethebullet.android.token.datalayer.TokenDbAdapter;
import uk.co.bitethebullet.android.token.tokens.HotpToken;

public class PinManager {

	private static final String SALT = "EE08F4A6-8497-4330-8CD5-8A4ABD93CD46";
	public static final String PIN_CODE_SETTING = "PIN_CODE";
	
	public static Boolean hasPinDefined(Context c){
		SharedPreferences sharedPreferences =
				PreferenceManager.getDefaultSharedPreferences(c);

		return sharedPreferences.getString("securityLock", "0").equals("1");
	}
	
	public static Boolean validatePin(Context c, String pin){

		SharedPreferences sharedPreferences =
				PreferenceManager.getDefaultSharedPreferences(c);
		String storedPin = sharedPreferences.getString(PIN_CODE_SETTING, "");

		return storedPin.equals(pin);
	}
	
	public static void storePin(Context c, String pin){
		SharedPreferences sharedPreferences =
				PreferenceManager.getDefaultSharedPreferences(c);
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putString(PinManager.PIN_CODE_SETTING, pin);
		editor.commit();
	}
	
//	public static void removePin(Context c){
//		TokenDbAdapter db = new TokenDbAdapter(c);
//		db.open();
//
//		db.deletePin();
//
//		db.close();
//	}
	
	private static String createPinHash(String pin) {
		
		try{
			
			String toHash = SALT + pin;
			
			MessageDigest md = MessageDigest.getInstance("SHA1");
			md.reset();
			md.update(toHash.getBytes());
			byte[] hashOutput = md.digest();
			
			return HotpToken.byteArrayToHexString(hashOutput);
			
		}catch(NoSuchAlgorithmException ex){
			return null;
		}
		
	}
	
}
