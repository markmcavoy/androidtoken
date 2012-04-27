package uk.co.bitethebullet.android.token;

public class TokenMetaData implements ITokenMeta {

	public static final int HOTP_TOKEN = 0;
	public static final int TOTP_TOKEN = 1;
	
	String tokenName;
	int tokenType;
	String secretBase32;
	int digits;
	int timeStep;
	int counter;
	
	public TokenMetaData(String tokenName, int tokenType, String secret,
							int digits, int timeStep, int counter)
	{
		this.tokenName = tokenName;
		this.tokenType = tokenType;
		this.secretBase32 = secret;
		this.digits = digits;
		this.timeStep = timeStep;
		this.counter = counter;
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

}
