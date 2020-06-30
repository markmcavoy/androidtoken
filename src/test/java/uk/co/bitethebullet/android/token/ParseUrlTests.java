package uk.co.bitethebullet.android.token;

import junit.framework.Assert;
import uk.co.bitethebullet.android.token.ITokenMeta;
import uk.co.bitethebullet.android.token.OtpAuthUriException;
import uk.co.bitethebullet.android.token.TokenList;
import uk.co.bitethebullet.android.token.TokenMetaData;
import uk.co.bitethebullet.android.token.util.SeedConvertor;

import java.io.IOException;
import java.io.UTFDataFormatException;

import android.content.Context;
import android.test.InstrumentationTestCase;

public class ParseUrlTests extends InstrumentationTestCase {

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
		
		setContext(this.getInstrumentation().getTargetContext());
	}
	
//	void displayFiles (AssetManager mgr, String path) {
//	    try {
//	        String list[] = mgr.list(path);
//	        if (list != null)
//	            for (int i=0; i<list.length; ++i)
//	                {
//	                    Log.v("Assets:", path +"/"+ list[i]);
//	                    displayFiles(mgr, path + "/" + list[i]);
//	                }
//	    } catch (IOException e) {
//	        Log.v("List error:", "can't list" + path);
//	    }
//
//	}
	
	public void testHotp1() throws OtpAuthUriException{
		String url = "otpauth://hotp/alice@google.com?secret=JBSWY3DPEHPK3PXP&counter=10";
		
		ITokenMeta token = TokenList.parseOtpAuthUrl(getContext(), url);
		
		Assert.assertEquals("alice@google.com", token.getName());
		Assert.assertEquals("JBSWY3DPEHPK3PXP", token.getSecretBase32());
		Assert.assertEquals(10, token.getCounter());
		Assert.assertEquals(6, token.getDigits());
		Assert.assertEquals(TokenMetaData.HOTP_TOKEN, token.getTokenType());
	}
	
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
		
		Assert.assertEquals("alice@google.com", token.getName());
		Assert.assertEquals("abcd12345678", new String(seed));
		Assert.assertEquals(10, token.getCounter());
		Assert.assertEquals(6, token.getDigits());
		Assert.assertEquals(TokenMetaData.HOTP_TOKEN, token.getTokenType());
	}
	
	
	public void testNotOathUrl(){
		String url = "http://www.bitethebullet.co.uk";
		
		try{
			ITokenMeta token = TokenList.parseOtpAuthUrl(getContext(), url);
			Assert.fail();
		}catch(OtpAuthUriException e){
			
		}
	}
	
	public void testNotValidTokenType(){
		String url = "otpauth://mark/alice@google.com?secret=JBSWY3DPEHPK3PXP";
		try{
			ITokenMeta token = TokenList.parseOtpAuthUrl(getContext(), url);
			Assert.fail();
		}catch(OtpAuthUriException e){
			
		}
	}
	
	public void testHotpMissingCounter(){
		String url = "otpauth://hotp/alice@google.com?secret=JBSWY3DPEHPK3PXP";
		
		try {
			ITokenMeta token = TokenList.parseOtpAuthUrl(getContext(), url);
			Assert.fail();
		} catch (OtpAuthUriException e) {
			e.printStackTrace();
		}		
	}
	
	public void testTotp() throws OtpAuthUriException{
		String url = "otpauth://totp/mark?secret=JBSWY3DPEHPK3PXP";
		
		ITokenMeta token = TokenList.parseOtpAuthUrl(getContext(), url);
		
		Assert.assertEquals("mark", token.getName());
		Assert.assertEquals("JBSWY3DPEHPK3PXP", token.getSecretBase32());
		Assert.assertEquals(6, token.getDigits());
		Assert.assertEquals(30, token.getTimeStep());
		Assert.assertEquals(TokenMetaData.TOTP_TOKEN, token.getTokenType());
	}
	
	public void testTotpPeriod() throws OtpAuthUriException{
		String url = "otpauth://totp/mark?secret=JBSWY3DPEHPK3PXP&period=60";
		
		ITokenMeta token = TokenList.parseOtpAuthUrl(getContext(), url);
		
		Assert.assertEquals("mark", token.getName());
		Assert.assertEquals("JBSWY3DPEHPK3PXP", token.getSecretBase32());
		Assert.assertEquals(6, token.getDigits());
		Assert.assertEquals(60, token.getTimeStep());
		Assert.assertEquals(TokenMetaData.TOTP_TOKEN, token.getTokenType());
	}
	
	public void testTotpDigits() throws OtpAuthUriException{
		String url = "otpauth://totp/mark?secret=JBSWY3DPEHPK3PXP&digits=8";
		
		ITokenMeta token = TokenList.parseOtpAuthUrl(getContext(), url);
		
		Assert.assertEquals("mark", token.getName());
		Assert.assertEquals("JBSWY3DPEHPK3PXP", token.getSecretBase32());
		Assert.assertEquals(8, token.getDigits());
		Assert.assertEquals(30, token.getTimeStep());
		Assert.assertEquals(TokenMetaData.TOTP_TOKEN, token.getTokenType());
	}
	
	public void testTotpPaddingTokenName() throws OtpAuthUriException, IOException{
		String url = "otpauth://totp/github.com/madeupuser?secret=mfrggzbrgiztinjwg44a====";
		
		ITokenMeta token = TokenList.parseOtpAuthUrl(getContext(), url);
		
		Assert.assertEquals("github.com/madeupuser", token.getName());
		Assert.assertEquals("abcd12345678", new String(SeedConvertor.ConvertFromEncodingToBA(token.getSecretBase32(), SeedConvertor.BASE32_FORMAT)));
		Assert.assertEquals(6, token.getDigits());
		Assert.assertEquals(30, token.getTimeStep());
		Assert.assertEquals(TokenMetaData.TOTP_TOKEN, token.getTokenType());
	}
	
}
