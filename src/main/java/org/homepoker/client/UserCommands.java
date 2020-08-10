package org.homepoker.client;

import java.io.IOException;

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

	@ShellMethod("Register a default user.")
    public void registerDefaultUser() {
        log.info("\nRegistering default user [test@test.com]...");
        User defaultUser = User.builder()
				.email("test@test.com")
				.alias("Fred")
				.name("Fred Jones")
				.phone("123 123 1234")
				.build();
        
        User user = connectionManager.getRsocketRequester()
                .route("register-user")
                .data(defaultUser)
                .retrieveMono(User.class)
                .block();
        log.info("\nResponse was: {}", user);
    }

	@ShellMethod("Register a user.")
    public void registerUser(User user) throws IOException {
		
         
        user = connectionManager.getRsocketRequester()
                .route("register-user")
                .data(user)
                .retrieveMono(User.class)
                .block();
        log.info("\nResponse was: {}", user);
    }
	
	@ShellMethodAvailability({"register-default-user", "register-user"})
    private Availability validConnection() {
    	return this.connectionManager.connectionAvailability();
    }
	
}
