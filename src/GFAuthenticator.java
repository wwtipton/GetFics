

import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.logging.Logger;

import com.notcomingsoon.getfics.GFLogger;

public class GFAuthenticator extends Authenticator {
	
	protected Logger logger = GFLogger.getLogger();
	
	private String user = "";
	
	private char[] password = null;

	
	protected PasswordAuthentication getPasswordAuthentication() {
		logger.info("Host:\t" + this.getRequestingHost());
		logger.info("User:\t" + user);
		logger.info("Password:\t" + password.toString());
		
		return new PasswordAuthentication(user, password);
	}


	public String getUser() {
		return user;
	}


	public void setUser(String user) {
		this.user = user;
	}


	public char[] getPassword() {
		return password;
	}


	public void setPassword(char[] password) {
		this.password = password;
	}

}
