package org.homepoker.client;

import java.time.Duration;

import javax.annotation.PreDestroy;

import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.shell.Availability;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class RSocketClientConnectionManager {
	private final RSocketRequester.Builder rsocketRequesterBuilder;
	private RSocketRequester rsocketRequester;

	public RSocketClientConnectionManager(RSocketRequester.Builder rsocketRequesterBuilder) {
		this.rsocketRequesterBuilder = rsocketRequesterBuilder;
	}

	public void connect(String host, Integer port) {
        log.info("\nConnecting to server...");
		this.rsocketRequester = rsocketRequesterBuilder.connectTcp(host, port).block(Duration.ofSeconds(10));
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
