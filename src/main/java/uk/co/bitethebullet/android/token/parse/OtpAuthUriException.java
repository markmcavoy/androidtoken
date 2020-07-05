package uk.co.bitethebullet.android.token.parse;

public class OtpAuthUriException extends Exception {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	String err;
	
	public OtpAuthUriException()
	  {
	    super();             // call superclass constructor
	    err = "unknown";
	  }
	  
	//-----------------------------------------------
	// Constructor receives some kind of message that is saved in an instance variable.
	  public OtpAuthUriException(String err)
	  {
	    super(err);     // call super class constructor
	    this.err = err;  // save message
	  }
}
