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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import uk.co.bitethebullet.android.token.util.Hex;

import android.content.Context;
import android.database.Cursor;

public class PinManager {

	private static final String SALT = "EE08F4A6-8497-4330-8CD5-8A4ABD93CD46";
	private static final String MASK = "GBKOISIIHVE7WBEI7O33KQY7CI";

    private static byte[] hashWith(String salt, String pin) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA1");
            md.reset();
            md.update(salt.getBytes());
            md.update(pin.getBytes());
            return md.digest();
		}catch(NoSuchAlgorithmException ex){
			return null;
		}
    }
	
	public static Boolean hasPinDefined(Context c){
		TokenDbAdapter db = new TokenDbAdapter(c);
		db.open();
		
		Cursor cursor = db.fetchPin();
		
        boolean hasPin = false;
        if (cursor != null) {
            hasPin = true;
            cursor.close();
        }

		db.close();
		
		return hasPin;
	}
	
	private static String createPinHash(String pin) {
        return Hex.byteArrayToHex(hashWith(SALT, pin));
	}

    private static byte[] getSeedMask(String pin) {
        if (pin == null)
            return new byte[0];

        // Rather than using the pin itself, we hash it (with a different salt) to get a better-distributed hex string
        return hashWith(MASK, pin);
    }

	public static byte[] validatePinGetSeedMask(Context c, String pin){

		boolean isValid;
        boolean maskSeed = false;

		TokenDbAdapter db = new TokenDbAdapter(c);
		db.open();
		Cursor cursor = db.fetchPin();
		
		if (cursor == null)
            // if no pin hash we treat empty string as correct
            isValid = pin == null || pin.isEmpty();
        else {
            int hashidx = cursor.getColumnIndexOrThrow(TokenDbAdapter.KEY_PIN_HASH);
            // if null hash, accept any pin
            if (cursor.isNull(hashidx))
                isValid = true;
            else {
                String dbPin = cursor.getString(hashidx);
                String userPin = createPinHash(pin);
                isValid = dbPin.contentEquals(userPin);
            }

            maskSeed = cursor.getInt(cursor.getColumnIndexOrThrow(TokenDbAdapter.KEY_PIN_MASK_SEED)) > 0;
            cursor.close();
		}
		
		db.close();
		
        if (isValid)
            return getSeedMask(maskSeed ? pin : null);
        else
            return null;
	}

	public static byte[] changePin(Context c, String old, String pin, boolean maskSeed, boolean validate){
        byte[] oldmask = validatePinGetSeedMask(c, old);
        if (oldmask == null)
            return null;

        byte[] mask = getSeedMask(maskSeed ? pin : null);
        String hash = validate ? createPinHash(pin) : null;

		TokenDbAdapter db = new TokenDbAdapter(c);
		db.open();

        if (pin == null)
            db.deletePin();
        else
            db.createOrUpdatePin(hash, maskSeed);

        if (!Arrays.equals(oldmask, mask)) {
            /* need to update all seeds with the new mask */
            Cursor cursor = db.fetchAllTokens();
            int rowidx = cursor.getColumnIndexOrThrow(TokenDbAdapter.KEY_TOKEN_ROWID);
            int seedidx = cursor.getColumnIndexOrThrow(TokenDbAdapter.KEY_TOKEN_SEED);
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                String seed = cursor.getString(seedidx);
                seed = HotpToken.maskSeed(seed, oldmask);
                seed = HotpToken.maskSeed(seed, mask);
                db.updateToken(cursor.getLong(rowidx), seed);
                cursor.moveToNext();
            }
            cursor.close();
        }

		db.close();

        return mask;
	}

	public static boolean removePin(Context c, String old){
        return changePin(c, old, null, false, false) != null;
    }
}
