package JuliaLang;

import com.jcraft.jsch.UserInfo;
import java.util.logging.Logger;;

 

public class JBoxUserInfo implements UserInfo {

	public static final Logger LOGGER = Logger.getLogger(JBoxUserInfo.class.getName());
	
	String password;
	String passphrase;
	
	public JBoxUserInfo(String password){
		this.password=password;
		this.passphrase=password;
		
	}
		
	public String getPassphrase() {
		return passphrase;
	}

	public String getPassword() {
		return password;
	}

	public boolean promptPassphrase(String arg0) {
		return false;
	}

	public boolean promptPassword(String arg0) {
		return false;
	}

	public boolean promptYesNo(String arg0) {
		return false;
	}

	public void showMessage(String arg0) {
		LOGGER.info(arg0);
	}

}
