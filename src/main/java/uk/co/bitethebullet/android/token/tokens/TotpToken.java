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

import java.io.IOException;
import java.util.Calendar;
import java.util.TimeZone;

import uk.co.bitethebullet.android.token.datalayer.TokenDbAdapter;
import uk.co.bitethebullet.android.token.util.SeedConvertor;


/**
 * TOTP Token
 * 
 * Generates an OTP based on the time, for more information see
 * http://tools.ietf.org/html/draft-mraihi-totp-timebased-00
 * 
 */
public class TotpToken extends HotpToken {

	private int mTimeStep;
	
	public TotpToken(String name, String serial, String seed, int timeStep,
					 	int otpLength, String organisation){
		super(name, serial, seed, 0, otpLength, organisation);
		
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
		return generateOtp(Calendar.getInstance(TimeZone.getTimeZone("UTC")));
	}

	@Override
	public String getUrl() {
		//otpauth://totp/organisation:alice@google.com?secret=JBSWY3DPEHPK3PXP"
		try {
			//convert the seed from hex to base32
			String base32Secret = SeedConvertor.ConvertFromBA(SeedConvertor.ConvertFromEncodingToBA(getSeed(), SeedConvertor.HEX_FORMAT),
					SeedConvertor.BASE32_FORMAT);

			StringBuilder buffer = new StringBuilder();
			buffer.append("otpauth://totp/");

			if (getOrganisation() != null && getOrganisation().length() > 0) {
				buffer.append(java.net.URLEncoder.encode(getOrganisation()));
				buffer.append(":");
			}

			buffer.append(java.net.URLEncoder.encode(getName()));
			buffer.append("?secret=");
			buffer.append(base32Secret);

			return buffer.toString();
		}catch(IOException ex){
			return null;
		}
	}

	public String generateOtp(Calendar currentTime){
		long time = currentTime.getTimeInMillis()/1000L;
		super.setEventCount(time/new Long(mTimeStep));
		
		return super.generateOtp();
	}
	
	
}
