package org.homepoker.client;

import javax.annotation.PreDestroy;

import org.homepoker.domain.user.User;
import org.homepoker.domain.user.UserInformationUpdate;
import org.springframework.beans.factory.ObjectProvider;
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

	/**
	 * Note the RSocketRequester.Builder is a prototype scope bean and since we
	 * we may use a builder multiple times within this service, we need to make sure
	 * we get a "fresh" instance of the builder each time we use it to construct
	 * the RSocketRequester. So we use an ObjectProvider to get a new instance each time.
	 */
	private final ObjectProvider<RSocketRequester.Builder> rsocketRequesterBuilder;

	
	private RSocketRequester rsocketRequester;
	private User currentUser = null;
	
	public RSocketClientConnectionManager(ObjectProvider<RSocketRequester.Builder> rsocketRequesterBuilder) {
		this.rsocketRequesterBuilder = rsocketRequesterBuilder;
	}

	/**
	 * Connect to the server as an anonymous user. This is allowed so a user may register with the server.
	 * The only thing an anonymous user is allowed to do is connect and register.
	 * 
	 * @param host Poker server host
	 * @param port Poker server port
	 */
	public void connect(String host, Integer port) {
		disconnect();
        log.info("\nConnecting to server...");
		this.rsocketRequester = getRsocketRequesterBuilder()
				.rsocketStrategies(builder-> builder.encoder(new SimpleAuthenticationEncoder()))
				.connectTcp(host, port)
				.log()
				.block();
        log.info("\nConnected to {}:{} as anonymous", host, port);
	}

	/**
	 * Connect to the server as a specific user. 
	 * 
	 * @param host Poker server host
	 * @param port Poker server port
	 */
	public void connect(String host, Integer port, String userId, String password) {
		disconnect();
        log.info("\nConnecting to server...");
        UsernamePasswordMetadata userMeta = new UsernamePasswordMetadata(userId, password);
		this.rsocketRequester = getRsocketRequesterBuilder()
				.rsocketStrategies(builder-> builder.encoder(new SimpleAuthenticationEncoder()))
				.setupMetadata(userMeta, SIMPLE_AUTH)
				.connectTcp(host, port)
				.block();
		
		currentUser = this.rsocketRequester
		    .route("user-manager-get-user")
		    .data(userId)
		    .retrieveMono(User.class)
			.doOnError( error -> rsocketRequester = null)
			.doOnSuccess(user -> log.info("\nConnected to {}:{} as {}", host, port, userId))
			.block();
		
	}
	
	/**
	 * Update the user's basic contact information. You cannot update a user's password with this method.
	 * @param userInformation
	 */
	public void updateUser(UserInformationUpdate userInformation) {
		User user = rsocketRequester
                .route("user-manager-update-user")
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
		currentUser = null;
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
	
	private RSocketRequester.Builder getRsocketRequesterBuilder() {
		return rsocketRequesterBuilder.getObject();
	}
}
