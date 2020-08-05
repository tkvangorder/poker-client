package org.homepoker.client;

import org.homepoker.domain.user.User;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@ShellComponent
public class PokerShellClient {
	
	private final RSocketRequester rsocketRequester;

	public PokerShellClient(RSocketRequester.Builder rsocketRequesterBuilder) {
		this.rsocketRequester = rsocketRequesterBuilder.connectTcp("localhost", 7000).block();
	}
		
    @ShellMethod("Create a user")
    public void createDefaultUser() throws InterruptedException {
        log.info("\nCreating default user...");
        User defaultUser = User.builder()
				.email("test@test.com")
				.alias("Fred")
				.name("Fred Jones")
				.phone("123 123 1234")
				.build();
        
        User user = this.rsocketRequester
                .route("create-user")
                .data(defaultUser)
                .retrieveMono(User.class)
                .block();
        log.info("\nResponse was: {}", user);
    }
	
}
