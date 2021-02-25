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
package uk.co.bitethebullet.android.token.util;

import java.io.IOException;
import uk.co.bitethebullet.android.token.tokens.HotpToken;

public class SeedConvertor {
	
	public static final int HEX_FORMAT = 0;
	public static final int BASE32_FORMAT = 1;
	public static final int BASE64_FORMAT = 2;
	
	public static byte[] ConvertFromEncodingToBA(String input, int currentFormat) throws IOException{
		
		if(currentFormat == 0){
			//hex
			return HotpToken.stringToHex(input);
		}else if(currentFormat == 1){
			//base 32
			Base32 base32 = new Base32();
			return base32.decodeBytes(input.toUpperCase());
		}else if(currentFormat == 2){
			//base64
			return Base64.decode(input);
		}else
			return null;
	}
	
	public static String ConvertFromBA(byte[] input, int targetFormat){
		if(targetFormat == 0){
			//hex
			return HotpToken.byteArrayToHexString(input);
		}else if(targetFormat == 1){
			//base 32
			Base32 base32 = new Base32();
			return base32.encodeBytes(input);
		}else if(targetFormat == 2){
			//base64
			return Base64.encodeBytes(input);
		}else
			return null;
	}
}
