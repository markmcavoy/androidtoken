package uk.co.bitethebullet.android.token;

import org.junit.Assert;
import org.junit.Test;
import com.google.common.truth.Truth;

import uk.co.bitethebullet.android.token.parse.OtpAuthUriException;
import uk.co.bitethebullet.android.token.parse.UrlParser;
import uk.co.bitethebullet.android.token.tokens.ITokenMeta;
import uk.co.bitethebullet.android.token.tokens.TokenMetaData;
import uk.co.bitethebullet.android.token.util.SeedConvertor;

import java.io.IOException;

import android.content.Context;

public class ParseUrlTests {

	Context c;
	
	public ParseUrlTests() {
	}
	
	protected Context getContext(){
		return c;
	}

	@Test
	public void testHotp1() throws OtpAuthUriException{
		String url = "otpauth://hotp/alice@google.com?secret=JBSWY3DPEHPK3PXP&counter=10";
		
		ITokenMeta token = UrlParser.parseOtpAuthUrl(url);

		Truth.assertThat(token.getName()).isEqualTo("alice@google.com");
		Truth.assertThat(token.getSecretBase32()).isEqualTo("JBSWY3DPEHPK3PXP");
		Truth.assertThat(token.getCounter()).isEqualTo(10);
		Truth.assertThat(token.getTokenType()).isEqualTo(TokenMetaData.HOTP_TOKEN);
		Truth.assertThat(token.getOrganisation()).isNull();
	}

	@Test
	public void testHotpWithPadding() throws OtpAuthUriException{
		String url = "otpauth://hotp/alice@google.com?secret=MFRGGZBRGIZTINJWG44A====&counter=10";
		
		ITokenMeta token = UrlParser.parseOtpAuthUrl(url);
		
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
		Truth.assertThat(token.getOrganisation()).isNull();
	}


	@Test
	public void testNotOathUrl(){
		String url = "http://www.bitethebullet.co.uk";
		
		try{
			ITokenMeta token = UrlParser.parseOtpAuthUrl(url);
			Assert.fail();
		}catch(OtpAuthUriException e){
			
		}
	}

	@Test
	public void testNotValidTokenType(){
		String url = "otpauth://mark/alice@google.com?secret=JBSWY3DPEHPK3PXP";
		try{
			ITokenMeta token = UrlParser.parseOtpAuthUrl(url);
			Assert.fail();
		}catch(OtpAuthUriException e){
			
		}
	}

	@Test
	public void testHotpMissingCounter(){
		String url = "otpauth://hotp/alice@google.com?secret=JBSWY3DPEHPK3PXP";
		
		try {
			ITokenMeta token = UrlParser.parseOtpAuthUrl(url);
			Assert.fail();
		} catch (OtpAuthUriException e) {
			//e.printStackTrace();
		}		
	}

	@Test
	public void testTotp() throws OtpAuthUriException{
		String url = "otpauth://totp/mark?secret=JBSWY3DPEHPK3PXP";
		
		ITokenMeta token = UrlParser.parseOtpAuthUrl(url);

		Truth.assertThat(token.getName()).isEqualTo("mark");
		Truth.assertThat(token.getSecretBase32()).isEqualTo("JBSWY3DPEHPK3PXP");
		Truth.assertThat(token.getTimeStep()).isEqualTo(30);
		Truth.assertThat(token.getTokenType()).isEqualTo(TokenMetaData.TOTP_TOKEN);
		Truth.assertThat(token.getOrganisation()).isNull();
	}

	@Test
	public void testTotpPeriod() throws OtpAuthUriException{
		String url = "otpauth://totp/mark?secret=JBSWY3DPEHPK3PXP&period=60";
		
		ITokenMeta token = UrlParser.parseOtpAuthUrl(url);

		Truth.assertThat(token.getName()).isEqualTo("mark");
		Truth.assertThat(token.getSecretBase32()).isEqualTo("JBSWY3DPEHPK3PXP");
		Truth.assertThat(token.getTimeStep()).isEqualTo(60);
		Truth.assertThat(token.getTokenType()).isEqualTo(TokenMetaData.TOTP_TOKEN);
		Truth.assertThat(token.getOrganisation()).isNull();
	}

	@Test
	public void testTotpDigits() throws OtpAuthUriException{
		String url = "otpauth://totp/mark?secret=JBSWY3DPEHPK3PXP&digits=8";
		
		ITokenMeta token = UrlParser.parseOtpAuthUrl(url);

		Truth.assertThat(token.getName()).isEqualTo("mark");
		Truth.assertThat(token.getSecretBase32()).isEqualTo("JBSWY3DPEHPK3PXP");
		Truth.assertThat(token.getTimeStep()).isEqualTo(30);
		Truth.assertThat(token.getDigits()).isEqualTo(8);
		Truth.assertThat(token.getTokenType()).isEqualTo(TokenMetaData.TOTP_TOKEN);
		Truth.assertThat(token.getOrganisation()).isNull();
	}

	@Test
	public void testTotpPaddingTokenName() throws OtpAuthUriException, IOException{
		String url = "otpauth://totp/github.com/madeupuser?secret=mfrggzbrgiztinjwg44a====";
		
		ITokenMeta token = UrlParser.parseOtpAuthUrl(url);

		Truth.assertThat(token.getName()).isEqualTo("github.com/madeupuser");
		Truth.assertThat(new String(SeedConvertor.ConvertFromEncodingToBA(token.getSecretBase32(), SeedConvertor.BASE32_FORMAT)))
						.isEqualTo("abcd12345678");
		Truth.assertThat(token.getTimeStep()).isEqualTo(30);
		Truth.assertThat(token.getDigits()).isEqualTo(6);
		Truth.assertThat(token.getTokenType()).isEqualTo(TokenMetaData.TOTP_TOKEN);
		Truth.assertThat(token.getOrganisation()).isNull();
	}


	@Test
	public void testTotpOrganisationName1() throws OtpAuthUriException{
		String url = "otpauth://totp/Example:alice@gmail.com?secret=mfrggzbrgiztinjwg44a====";
		ITokenMeta token = UrlParser.parseOtpAuthUrl(url);

		Truth.assertThat(token.getOrganisation()).isEqualTo("Example");
		Truth.assertThat(token.getName()).isEqualTo(("alice@gmail.com"));
	}

	@Test
	public void testTotpOrganisationName2() throws OtpAuthUriException{
		String url = "otpauth://totp/Provider1:Alice%20Smith?secret=mfrggzbrgiztinjwg44a====";
		ITokenMeta token = UrlParser.parseOtpAuthUrl(url);

		Truth.assertThat(token.getOrganisation()).isEqualTo("Provider1");
		Truth.assertThat(token.getName()).isEqualTo(("Alice Smith"));
	}

	@Test
	public void testTotpOrganisationName3() throws OtpAuthUriException{
		String url = "otpauth://totp/Big%20Corporation%3A%20alice%40bigco.com?secret=mfrggzbrgiztinjwg44a====";
		ITokenMeta token = UrlParser.parseOtpAuthUrl(url);

		Truth.assertThat(token.getOrganisation()).isEqualTo("Big Corporation");
		Truth.assertThat(token.getName()).isEqualTo(("alice@bigco.com"));
	}
	
}
