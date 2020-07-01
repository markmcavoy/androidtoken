package uk.co.bitethebullet.android.token;

import org.junit.Assert;
import org.junit.Test;
import com.google.common.truth.Truth;

import uk.co.bitethebullet.android.token.ITokenMeta;
import uk.co.bitethebullet.android.token.OtpAuthUriException;
import uk.co.bitethebullet.android.token.TokenList;
import uk.co.bitethebullet.android.token.TokenMetaData;
import uk.co.bitethebullet.android.token.util.SeedConvertor;

import java.io.IOException;
import java.io.UTFDataFormatException;

import android.content.Context;

public class ParseUrlTests {

	Context c;
	
	public ParseUrlTests() {
	}
	
	protected Context getContext(){
		return c;
	}
	
	protected void setContext(Context c){
		this.c = c;
	}

	protected void setUp(){
//		Context c = this.getInstrumentation().getTargetContext();
		
		//String s = c.getApplicationContext().getString(uk.co.bitethebullet.android.token.R.string.otpAuthMissingCounterParameter);
//		displayFiles(c.getApplicationContext().getAssets(), "/");
		
		//setContext(this.getInstrumentation().getTargetContext());
	}


	@Test
	public void testHotp1() throws OtpAuthUriException{
		String url = "otpauth://hotp/alice@google.com?secret=JBSWY3DPEHPK3PXP&counter=10";
		
		ITokenMeta token = TokenList.parseOtpAuthUrl(getContext(), url);

		Truth.assertThat(token.getName()).isEqualTo("alice@google.com");
		Truth.assertThat(token.getSecretBase32()).isEqualTo("JBSWY3DPEHPK3PXP");
		Truth.assertThat(token.getCounter()).isEqualTo(10);
		Truth.assertThat(token.getTokenType()).isEqualTo(TokenMetaData.HOTP_TOKEN);
	}

	@Test
	public void testHotpWithPadding() throws OtpAuthUriException{
		String url = "otpauth://hotp/alice@google.com?secret=MFRGGZBRGIZTINJWG44A====&counter=10";
		
		ITokenMeta token = TokenList.parseOtpAuthUrl(getContext(), url);
		
		//the above base32 input should equate to 'abcd12345678'
		byte[] seed = null;
		try {
			seed = SeedConvertor.ConvertFromEncodingToBA(token.getSecretBase32(), SeedConvertor.BASE32_FORMAT);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Truth.assertThat(token.getName()).isEqualTo("alice@google.com");
		Truth.assertThat(new String(seed)).isEqualTo("abcd12345678");
		Truth.assertThat(token.getCounter()).isEqualTo(10);
		Truth.assertThat(token.getTokenType()).isEqualTo(TokenMetaData.HOTP_TOKEN);
	}


	@Test
	public void testNotOathUrl(){
		String url = "http://www.bitethebullet.co.uk";
		
		try{
			ITokenMeta token = TokenList.parseOtpAuthUrl(getContext(), url);
			Assert.fail();
		}catch(OtpAuthUriException e){
			
		}
	}

	@Test
	public void testNotValidTokenType(){
		String url = "otpauth://mark/alice@google.com?secret=JBSWY3DPEHPK3PXP";
		try{
			ITokenMeta token = TokenList.parseOtpAuthUrl(getContext(), url);
			Assert.fail();
		}catch(OtpAuthUriException e){
			
		}
	}

	@Test
	public void testHotpMissingCounter(){
		String url = "otpauth://hotp/alice@google.com?secret=JBSWY3DPEHPK3PXP";
		
		try {
			ITokenMeta token = TokenList.parseOtpAuthUrl(getContext(), url);
			Assert.fail();
		} catch (OtpAuthUriException e) {
			//e.printStackTrace();
		}		
	}

	@Test
	public void testTotp() throws OtpAuthUriException{
		String url = "otpauth://totp/mark?secret=JBSWY3DPEHPK3PXP";
		
		ITokenMeta token = TokenList.parseOtpAuthUrl(getContext(), url);

		Truth.assertThat(token.getName()).isEqualTo("mark");
		Truth.assertThat(token.getSecretBase32()).isEqualTo("JBSWY3DPEHPK3PXP");
		Truth.assertThat(token.getTimeStep()).isEqualTo(30);
		Truth.assertThat(token.getTokenType()).isEqualTo(TokenMetaData.TOTP_TOKEN);
	}

	@Test
	public void testTotpPeriod() throws OtpAuthUriException{
		String url = "otpauth://totp/mark?secret=JBSWY3DPEHPK3PXP&period=60";
		
		ITokenMeta token = TokenList.parseOtpAuthUrl(getContext(), url);

		Truth.assertThat(token.getName()).isEqualTo("mark");
		Truth.assertThat(token.getSecretBase32()).isEqualTo("JBSWY3DPEHPK3PXP");
		Truth.assertThat(token.getTimeStep()).isEqualTo(60);
		Truth.assertThat(token.getTokenType()).isEqualTo(TokenMetaData.TOTP_TOKEN);
	}

	@Test
	public void testTotpDigits() throws OtpAuthUriException{
		String url = "otpauth://totp/mark?secret=JBSWY3DPEHPK3PXP&digits=8";
		
		ITokenMeta token = TokenList.parseOtpAuthUrl(getContext(), url);

		Truth.assertThat(token.getName()).isEqualTo("mark");
		Truth.assertThat(token.getSecretBase32()).isEqualTo("JBSWY3DPEHPK3PXP");
		Truth.assertThat(token.getTimeStep()).isEqualTo(30);
		Truth.assertThat(token.getDigits()).isEqualTo(8);
		Truth.assertThat(token.getTokenType()).isEqualTo(TokenMetaData.TOTP_TOKEN);
	}

	@Test
	public void testTotpPaddingTokenName() throws OtpAuthUriException, IOException{
		String url = "otpauth://totp/github.com/madeupuser?secret=mfrggzbrgiztinjwg44a====";
		
		ITokenMeta token = TokenList.parseOtpAuthUrl(getContext(), url);

		Truth.assertThat(token.getName()).isEqualTo("github.com/madeupuser");
		Truth.assertThat(new String(SeedConvertor.ConvertFromEncodingToBA(token.getSecretBase32(), SeedConvertor.BASE32_FORMAT)))
						.isEqualTo("abcd12345678");
		Truth.assertThat(token.getTimeStep()).isEqualTo(30);
		Truth.assertThat(token.getDigits()).isEqualTo(6);
		Truth.assertThat(token.getTokenType()).isEqualTo(TokenMetaData.TOTP_TOKEN);
	}
	
}
