package org.homepoker.client;

import org.springframework.shell.Availability;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellMethodAvailability;
import org.springframework.shell.standard.ShellOption;

@ShellComponent
public class PokerConnectCommands {
	
	private final RSocketClientConnectionManager connectionManager;
	private String host = "localhost";
	private Integer port = 7000;
	
	public PokerConnectCommands(RSocketClientConnectionManager connectionManager) {
		this.connectionManager = connectionManager;
	}

    @ShellMethod("Connect to a poker server.")
    public void connect( 
    		@ShellOption(defaultValue = "localhost") String host,
    		@ShellOption(defaultValue = "7000") Integer port) {
    	
    	this.host = host;
    	this.port = port;
    	connectionManager.connect(host, port);	
    }

    @ShellMethod("Login to existing poker server with user/password")
    public void connectWithUser(
			@ShellOption(help="User ID") String user,
			@ShellOption(help="User Password") String password,
			@ShellOption(defaultValue = "localhost") String host,
			@ShellOption(defaultValue = "7000") Integer port) {
    	
    	this.host = host;
    	this.port = port;
    	
    	connectionManager.connect(host, port, user, password);	
    }

    @ShellMethod("Login to existing poker server with user/password")
    public void login(String user, String password) {
    	connectionManager.connect(host, port, user, password);	
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
