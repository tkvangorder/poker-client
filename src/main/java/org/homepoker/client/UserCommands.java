package org.homepoker.client;

import org.homepoker.domain.user.User;
import org.springframework.shell.Availability;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellMethodAvailability;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@ShellComponent
public class UserCommands {

	private final RSocketClientConnectionManager connectionManager;
	public UserCommands(RSocketClientConnectionManager connectionManager) {
		this.connectionManager = connectionManager;
	}

	@ShellMethod("Create a default user.")
    public void createDefaultUser() throws InterruptedException {
        log.info("\nCreating default user...");
        User defaultUser = User.builder()
				.email("test@test.com")
				.alias("Fred")
				.name("Fred Jones")
				.phone("123 123 1234")
				.build();
        
        User user = connectionManager.getRsocketRequester()
                .route("create-user")
                .data(defaultUser)
                .retrieveMono(User.class)
                .block();
        log.info("\nResponse was: {}", user);
    }

	@ShellMethod("Create a user.")
    public void createUser(User user) throws InterruptedException {
         
        user = connectionManager.getRsocketRequester()
                .route("create-user")
                .data(user)
                .retrieveMono(User.class)
                .block();
        log.info("\nResponse was: {}", user);
    }
	
	@ShellMethodAvailability({"create-default-user", "create-user"})
    private Availability validConnection() {
    	if (!this.connectionManager.isConnected()) {
    		return Availability.unavailable("You are not connected to the server.");
    	} else {
    		return Availability.available();
    	}
    }
	
}
