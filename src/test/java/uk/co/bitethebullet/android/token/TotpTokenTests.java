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

import org.junit.Ignore;
import org.junit.Test;
import uk.co.bitethebullet.android.token.TotpToken;
import junit.framework.Assert;
import junit.framework.TestCase;

public class TotpTokenTests {

	private static final String SEED = "3132333435363738393031323334353637383930";

	@Test
	public void testOtp1(){
		//utc = 2005-03-18T01:58:31
		TotpToken token = new TotpToken("markTest", "1234", SEED, 30, 6);
		
		Calendar time = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
		time.set(2005, 2, 18, 1, 58, 31);
		
		System.out.println(time.getTime().toString());
		
		String otp = token.generateOtp(time);
		
		Assert.assertEquals("050471", otp);
	}

	@Test
	//@Ignore("TODO: This test fails as unit test but succeeds as integration test")
	public void testOtp2(){
		//utc  = 2009-02-13T23:31:30
		TotpToken token = new TotpToken("markTest", "1234", SEED, 30, 6);
		
		Calendar time = Calendar.getInstance();
		time.set(2009, 1, 13, 23, 31, 30);	
		
		String otp = token.generateOtp(time);
		
		Assert.assertEquals("005924", otp);
	}

	@Test
	//@Ignore("TODO: This test fails as unit test but succeeds as integration test")
	public void testOtp3(){
		//todo: MM this test fails, double check that the expected value should
		//be then fix as required!!
		//utc = 2033-05-18T03:33:20
		TotpToken token = new TotpToken("markTest", "1234", SEED, 30, 6);
		
		Calendar time = Calendar.getInstance();
		time.set(2033, 4, 18, 3, 33, 20);	
		
		String otp = token.generateOtp(time);
		
		Assert.assertEquals("279037", otp);
	}
}
