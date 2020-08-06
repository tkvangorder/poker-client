package org.homepoker.client;

import java.time.Duration;

import org.springframework.messaging.rsocket.RSocketRequester;
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
	boolean isConnected() {
		return this.rsocketRequester != null;
	}

	void disconnect() {
    	this.rsocketRequester.rsocket().dispose();
    	this.rsocketRequester = null;    			
	}
	
	public RSocketRequester getRsocketRequester() {
		return rsocketRequester;
	}
}
