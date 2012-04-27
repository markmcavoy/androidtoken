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

import java.util.Calendar;
import java.util.TimeZone;


/**
 * TOTP Token
 * 
 * Generates an OTP based on the time, for more information see
 * http://tools.ietf.org/html/draft-mraihi-totp-timebased-00
 * 
 */
public class TotpToken extends HotpToken {

	private int mTimeStep;
	
	public TotpToken(String name, String serial, String seed, int timeStep, int otpLength){
		super(name, serial, seed, 0, otpLength);
		
		mTimeStep = timeStep;
	}

	@Override
	public int getTimeStep(){
		return mTimeStep;
	}
	
	@Override
	public int getTokenType(){
		return TokenDbAdapter.TOKEN_TYPE_TIME;
	}
	
	@Override
	public String generateOtp() {
		
		//calculate the moving counter using the time
		return generateOtp(Calendar.getInstance(TimeZone.getTimeZone("GMT")));	
	}
	
	public String generateOtp(Calendar currentTime){
		long time =currentTime.getTimeInMillis()/1000;		
		super.setEventCount(time/mTimeStep);
		
		return super.generateOtp();
	}
	
	
}
