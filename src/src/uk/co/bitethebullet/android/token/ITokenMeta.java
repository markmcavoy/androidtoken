package uk.co.bitethebullet.android.token;

public interface ITokenMeta {

	public String getName();
	
	public int getTokenType();
	
	public String getSecretBase32();
	
	public int getDigits();
	
	public int getTimeStep();
	
	public int getCounter();
}
