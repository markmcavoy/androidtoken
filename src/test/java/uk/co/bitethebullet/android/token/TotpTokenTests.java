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

import java.util.Calendar;
import java.util.TimeZone;

import org.junit.Test;
import uk.co.bitethebullet.android.token.tokens.TotpToken;
import com.google.common.truth.Truth;


public class TotpTokenTests {

	private static final String SEED = "3132333435363738393031323334353637383930";

	@Test
	public void testOtp1(){
		//utc = 2005-03-18T01:58:31
		TotpToken token = new TotpToken("markTest", "1234", SEED, 30, 6);
		
		Calendar time = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		time.set(2005, 2, 18, 1, 58, 31);
		
		System.out.println(time.getTime().toString());
		String otp = token.generateOtp(time);

		Truth.assertThat(otp).isEqualTo("050471");
	}

	@Test
	public void testOtp2(){
		//utc  = 2009-02-13T23:31:30
		TotpToken token = new TotpToken("markTest", "1234", SEED, 30, 6);
		
		Calendar time = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		time.set(2009, 1, 13, 23, 31, 30);	
		
		String otp = token.generateOtp(time);

		Truth.assertThat(otp).isEqualTo("005924");
	}

	@Test
	public void testOtp3(){
		//utc = 2033-05-18T03:33:20
		//this would be counter = 66666666
		TotpToken token = new TotpToken("markTest", "1234", "3132333435363738393031323334353637383930", 30, 8);
		
		Calendar time = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		time.set(2033, 4, 18, 3, 33, 20);	
		
		String otp = token.generateOtp(time);

		Truth.assertThat(token.getEventCount()).isEqualTo(66666666L);
		Truth.assertThat(otp).isEqualTo("69279037");
	}


	@Test
	public void testOtp4(){
		//utc = 2603-10-11T11:33:20
		//this would be counter = 666666666
		TotpToken token = new TotpToken("markTest", "1234", "3132333435363738393031323334353637383930", 30, 8);

		Calendar time = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		time.set(2603, 9, 11, 11, 33, 20);

		String otp = token.generateOtp(time);

		Truth.assertThat(token.getEventCount()).isEqualTo(666666666L);
		Truth.assertThat(otp).isEqualTo("65353130");
	}

	@Test
	public void testOtp5(){
		//utc  = 2009-02-13T23:31:30
		TotpToken token = new TotpToken("markTest", "1234", "3132333435363738393031323334353637383930", 30, 8);

		Calendar time = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		time.set(2009, 1, 13, 23, 31, 30);

		String otp = token.generateOtp(time);

		Truth.assertThat(otp).isEqualTo("89005924");
	}
}
