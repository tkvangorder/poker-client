package org.homepoker.client;

import javax.annotation.PreDestroy;

import org.homepoker.domain.user.User;
import org.homepoker.domain.user.UserInformationUpdate;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.security.rsocket.metadata.SimpleAuthenticationEncoder;
import org.springframework.security.rsocket.metadata.UsernamePasswordMetadata;
import org.springframework.shell.Availability;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;

import io.rsocket.metadata.WellKnownMimeType;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class RSocketClientConnectionManager {
	private static final MimeType SIMPLE_AUTH = MimeTypeUtils.parseMimeType(WellKnownMimeType.MESSAGE_RSOCKET_AUTHENTICATION.getString());
	private static final User guestUser = User.builder().loginId("guest").build();
	private final RSocketRequester.Builder rsocketRequesterBuilder;
	private RSocketRequester rsocketRequester;
	private User currentUser = guestUser;
	
	public RSocketClientConnectionManager(RSocketRequester.Builder rsocketRequesterBuilder) {
		this.rsocketRequesterBuilder = rsocketRequesterBuilder;
	}

	public void connect(String host, Integer port) {
		disconnect();
        log.info("\nConnecting to server...");
		this.rsocketRequester = rsocketRequesterBuilder
				.rsocketStrategies(builder-> builder.encoder(new SimpleAuthenticationEncoder()))
				.connectTcp(host, port)
				.log()
				.block();
        log.info("\nConnected to {}:{} as anonymous", host, port);
	}

	public void connect(String host, Integer port, String userId, String password) {
		disconnect();
        log.info("\nConnecting to server...");
        UsernamePasswordMetadata userMeta = new UsernamePasswordMetadata(userId, password);
		this.rsocketRequester = rsocketRequesterBuilder
				.rsocketStrategies(builder-> builder.encoder(new SimpleAuthenticationEncoder()))
				.setupMetadata(userMeta, SIMPLE_AUTH) 
				.connectTcp(host, port)
				.block();
		
		currentUser = this.rsocketRequester
		    .route("get-user")
		    .data(userId)
		    .retrieveMono(User.class)
		    .block();
		
        log.info("\nConnected to {}:{} as {}", host, port, userId);		
	}
	
	public void updateUser(UserInformationUpdate userInformation) {
		User user = rsocketRequester
                .route("update-user")
                .data(userInformation)
                .retrieveMono(User.class)
                .block();
		
		currentUser = user;
	}
	
	Availability connectionAvailability() {
    	if (this.rsocketRequester == null) {
    		return Availability.unavailable("You are not connected to the server.");
    	} else {
    		return Availability.available();
    	}		
	}

	@PreDestroy
	void disconnect() {
		currentUser = guestUser;
		if (rsocketRequester != null) {
	    	this.rsocketRequester.rsocket().dispose();
	    	this.rsocketRequester = null;    			
		}
	}
	
	public RSocketRequester getRsocketRequester() {
		return rsocketRequester;
	}

	public User getCurrentUser() {
		return currentUser;
	}

}
