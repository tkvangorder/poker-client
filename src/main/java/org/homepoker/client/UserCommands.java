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
        		.loginId("test")
				.email("test@test.com")
				.password("fred")
				.alias("Fred")
				.name("Fred Jones")
				.phone("123 123 1234")
				.build();
        registerUser(defaultUser);
    }

	@ShellMethod("Register a user.")
    public void registerUser(User user) {
		
         
        user = connectionManager.getRsocketRequester()
                .route("register-user")
                .data(user)
                .retrieveMono(User.class)
                .block();
        log.info("\nResponse was: {}", user);
    }

	@ShellMethod("Delete a user.")
    public void deleteUser(String loginId) throws IOException {
		
         
        connectionManager.getRsocketRequester()
                .route("delete-user")
                .data(loginId)
                .retrieveMono(User.class)
                .block();
        log.info("\nUser [{}] has been deleted.", loginId);
    }
	
	@ShellMethodAvailability({"register-default-user", "register-user", "delete-user"})
    private Availability validConnection() {
    	return this.connectionManager.connectionAvailability();
    }
	
}
