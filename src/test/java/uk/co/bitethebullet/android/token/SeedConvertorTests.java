/*
 * Copyright Mark McAvoy - www.bitethebullet.co.uk 2011
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

import org.junit.Test;
import uk.co.bitethebullet.android.token.util.SeedConvertor;
import org.junit.Assert;

public class SeedConvertorTests  {

	String ascii = "bitethebullet.uk 1234";
	String hexValue = "6269746574686562756c6c65742e756b2031323334";
	String base32Value = "MJUXIZLUNBSWE5LMNRSXILTVNMQDCMRTGQ======";
	String base64Value = "Yml0ZXRoZWJ1bGxldC51ayAxMjM0";

	
	@Test
	public void testConvertHexToBA(){
		try {
			byte[] rawBytes = SeedConvertor.ConvertFromEncodingToBA(hexValue, 0);			
			byte[] expectedBytes = ascii.getBytes();
			
			for(int i = 0; i < rawBytes.length; i++){
				if(rawBytes[i] != expectedBytes[i])
					Assert.fail();
			}
			
			Assert.assertFalse(false);
			
		} catch (IOException e) {
			e.printStackTrace();
			Assert.fail();
		}
	}

	@Test
	public void testConvertBase32ToBA(){
		try {
			byte[] rawBytes = SeedConvertor.ConvertFromEncodingToBA(base32Value, 1);			
			byte[] expectedBytes = ascii.getBytes();
			
			for(int i = 0; i < rawBytes.length; i++){
				if(rawBytes[i] != expectedBytes[i])
					Assert.fail();
			}
			
			Assert.assertFalse(false);
			
		} catch (IOException e) {
			e.printStackTrace();
			Assert.fail();
		}
	}

	@Test
	public void testConvertLowerCaseBase32ToBA()
	{
		try {
			byte[] rawBytes = SeedConvertor.ConvertFromEncodingToBA(base32Value.toLowerCase(), 1);
			byte[] expectedBytes = ascii.getBytes();

			for(int i = 0; i < rawBytes.length; i++){
				if(rawBytes[i] != expectedBytes[i])
					Assert.fail();
			}

			Assert.assertFalse(false);

		} catch (IOException e) {
			e.printStackTrace();
			Assert.fail();
		}
	}

	@Test
	public void testConvertBase64ToBA(){
		try {
			byte[] rawBytes = SeedConvertor.ConvertFromEncodingToBA(base64Value, 2);			
			byte[] expectedBytes = ascii.getBytes();
			
			for(int i = 0; i < rawBytes.length; i++){
				if(rawBytes[i] != expectedBytes[i])
					Assert.fail();
			}
			
			Assert.assertFalse(false);
			
		} catch (IOException e) {
			e.printStackTrace();
			Assert.fail();
		}
	}
}
