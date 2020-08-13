package org.homepoker.client;

import org.springframework.shell.Availability;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellMethodAvailability;
import org.springframework.shell.standard.ShellOption;

@ShellComponent
public class PokerShellClient {
	
	private final RSocketClientConnectionManager connectionManager;
	public PokerShellClient(RSocketClientConnectionManager connectionManager) {
		this.connectionManager = connectionManager;
	}

    @ShellMethod("Connect to a poker server.")
    public void connect( 
    		@ShellOption(defaultValue = "localhost") String host,
    		@ShellOption(defaultValue = "7000") Integer port) {
    	connectionManager.connect(host, port);	
    }

    @ShellMethod("Login with user/password")
    public void login(String user, String password) {
//    	connectionManager.login(user, password);	
    }
    
    @ShellMethod("Disconnect from a poker server.")
    public void disconnect() {
    	connectionManager.disconnect();	
    }
	

    @ShellMethodAvailability({"disconnect"})
    private Availability validConnection() {
    	return this.connectionManager.connectionAvailability();
    }
}
