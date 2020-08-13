package org.homepoker.client;

import java.time.Duration;

import javax.annotation.PreDestroy;

import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.RSocketStrategies;
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
	 
	private final RSocketRequester.Builder rsocketRequesterBuilder;
	private RSocketRequester rsocketRequester;
	private final RSocketStrategies rsocketStrategies;
	
	public RSocketClientConnectionManager(RSocketRequester.Builder rsocketRequesterBuilder, RSocketStrategies rsocketStrategies) {
		this.rsocketRequesterBuilder = rsocketRequesterBuilder;
		this.rsocketStrategies = rsocketStrategies;
	}

	public void connect(String host, Integer port) {
        log.info("\nConnecting to server...");
        UsernamePasswordMetadata user = new UsernamePasswordMetadata("user", "pass");
		this.rsocketRequester = rsocketRequesterBuilder
				.setupMetadata(user, SIMPLE_AUTH)
				.rsocketStrategies(builder-> builder.encoder(new SimpleAuthenticationEncoder()))
				.connectTcp(host, port).block(Duration.ofSeconds(10));
        log.info("\nConnected to {}:{}", host, port);		
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
		if (rsocketRequester != null) {
	    	this.rsocketRequester.rsocket().dispose();
	    	this.rsocketRequester = null;    			
		}
	}
	
	public RSocketRequester getRsocketRequester() {
		return rsocketRequester;
	}
}
