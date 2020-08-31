package org.homepoker.client;

import java.io.IOException;

import org.homepoker.domain.user.User;
import org.homepoker.domain.user.UserInformationUpdate;
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
        log.info("\nRegistering default user [admin@test.com]...");
        User defaultUser = User.builder()
        		.loginId("admin")
				.email("admin@test.com")
				.password("admin")
				.alias("Mr Admin")
				.name("Administrator")
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

	public void updateUser(UserInformationUpdate userInformation) {
		connectionManager.updateUser(userInformation);
	}
	@ShellMethod("Delete a user.")
    public void deleteUser(String loginId) throws IOException {
		
         
        connectionManager.getRsocketRequester()
                .route("delete-user")
                .data(loginId)
                .retrieveMono(Void.class)
                .block();
        log.info("\nUser [{}] has been deleted.", loginId);
    }
	
	@ShellMethodAvailability({"register-default-user", "register-user", "delete-user"})
    private Availability validConnection() {
    	return this.connectionManager.connectionAvailability();
    }
	
}
