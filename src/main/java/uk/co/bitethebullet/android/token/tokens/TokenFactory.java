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
package uk.co.bitethebullet.android.token.tokens;

import android.database.Cursor;

import uk.co.bitethebullet.android.token.datalayer.TokenDbAdapter;

public class TokenFactory {

	/**
	 * Creates a IToken object from the database cursor
	 * @param c
	 * @param ctx
	 * @return
	 */
	public static IToken CreateToken(Cursor c){
		
		if(c == null){
			return null;
		}
		
		IToken token = null;
		
		int tokenType = c.getInt(c.getColumnIndexOrThrow(TokenDbAdapter.KEY_TOKEN_TYPE));
		
		switch(tokenType){
		
		case TokenDbAdapter.TOKEN_TYPE_EVENT:
			token = CreateHotpToken(c);
			break;
			
		case TokenDbAdapter.TOKEN_TYPE_TIME:
			token = CreateTotpToken(c);
			break;
		
		default:
			return null;
		}	
		
		token.setId(c.getLong(c.getColumnIndex(TokenDbAdapter.KEY_TOKEN_ROWID)));
		return token;
	}
	
	private static IToken CreateHotpToken(Cursor c){
		HotpToken token = new HotpToken(
								c.getString(c.getColumnIndexOrThrow(TokenDbAdapter.KEY_TOKEN_NAME)), 
								c.getString(c.getColumnIndexOrThrow(TokenDbAdapter.KEY_TOKEN_SERIAL)), 
								c.getString(c.getColumnIndexOrThrow(TokenDbAdapter.KEY_TOKEN_SEED)), 
								c.getLong(c.getColumnIndexOrThrow(TokenDbAdapter.KEY_TOKEN_COUNT)), 
								c.getInt(c.getColumnIndexOrThrow(TokenDbAdapter.KEY_TOKEN_OTP_LENGTH)),
								c.getString(c.getColumnIndexOrThrow(TokenDbAdapter.KEY_TOKEN_ORGANISATION)));
		
		return token;
	}
	
	private static IToken CreateTotpToken(Cursor c){
		TotpToken token = new TotpToken(
										c.getString(c.getColumnIndexOrThrow(TokenDbAdapter.KEY_TOKEN_NAME)), 
										c.getString(c.getColumnIndexOrThrow(TokenDbAdapter.KEY_TOKEN_SERIAL)), 
										c.getString(c.getColumnIndexOrThrow(TokenDbAdapter.KEY_TOKEN_SEED)), 
										c.getInt(c.getColumnIndexOrThrow(TokenDbAdapter.KEY_TOKEN_TIME_STEP)), 
										c.getInt(c.getColumnIndexOrThrow(TokenDbAdapter.KEY_TOKEN_OTP_LENGTH)),
										c.getString(c.getColumnIndexOrThrow(TokenDbAdapter.KEY_TOKEN_ORGANISATION)));
		
		return token;
	}
}
