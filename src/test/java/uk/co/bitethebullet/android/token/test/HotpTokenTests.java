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

package uk.co.bitethebullet.android.token.test;

import org.junit.Test;
import uk.co.bitethebullet.android.token.HotpToken;
import junit.framework.Assert;
import junit.framework.TestCase;

public class HotpTokenTests {

	private final static String SEED = "3132333435363738393031323334353637383930";

	@Test
	public void testOtpGeneration(){
		HotpToken token = new HotpToken("mark", "123456789", SEED, 0, 6);
		
		String otp = token.generateOtp();
		
		Assert.assertEquals("755224", otp);
	}

	@Test
	public void testOtpGeneration2(){
		HotpToken token = new HotpToken("mark", "123456789", SEED, 1, 6);
		
		String otp = token.generateOtp();
		
		Assert.assertEquals("287082", otp);
	}

	@Test
	public void testSeed128(){
		
		for(int i = 0; i < 100; i++){
		
			String seed = HotpToken.generateNewSeed(128);
			Assert.assertEquals(32, seed.length());			
		}
	}


	@Test
	public void testSeed160(){
		for(int i = 0; i < 100; i++){
			
			String seed = HotpToken.generateNewSeed(160);
			Assert.assertEquals(40, seed.length());			
		}
	}
}
