package uk.co.bitethebullet.android.token.tokens;

import uk.co.bitethebullet.android.token.tokens.ITokenMeta;

public class TokenMetaData implements ITokenMeta {

	public static final int HOTP_TOKEN = 0;
	public static final int TOTP_TOKEN = 1;
	
	String tokenName;
	String organisation;
	int tokenType;
	String secretBase32;
	int digits;
	int timeStep;
	int counter;

	public TokenMetaData(String tokenName, int tokenType, String secret,
						 int digits, int timeStep, int counter){

		this(tokenName, tokenType, secret, digits, timeStep, counter, null);
	}

	public TokenMetaData(String tokenName, int tokenType, String secret,
							int digits, int timeStep, int counter,
						 	String organisation)
	{
		this.tokenName = tokenName;
		this.tokenType = tokenType;
		this.secretBase32 = secret;
		this.digits = digits;
		this.timeStep = timeStep;
		this.counter = counter;
		this.organisation = organisation;
	}
	
	public String getName() {
		return tokenName;
	}

	public int getTokenType() {
		return tokenType;
	}

	public String getSecretBase32() {
		return secretBase32;
	}

	public int getDigits() {
		return digits;
	}

	public int getTimeStep() {
		return timeStep;
	}

	public int getCounter() {
		return counter;
	}

	public String getOrganisation(){ return organisation; }

}
