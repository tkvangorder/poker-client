package org.homepoker.client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveUserDetailsServiceAutoConfiguration;
import org.springframework.boot.autoconfigure.security.rsocket.RSocketSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

@SpringBootApplication(exclude = {ReactiveUserDetailsServiceAutoConfiguration.class, SecurityAutoConfiguration.class, ReactiveSecurityAutoConfiguration.class, RSocketSecurityAutoConfiguration.class})
public class PokerClientApplication {

	public static void main(String[] args) {
		SpringApplication.run(PokerClientApplication.class, args);
	}

	//This RSocket encoder is needed for marshaling simple authentication.
//	@Bean
//	RSocketStrategiesCustomizer rSocketStrategiesCustomizer() {
//		return strategies -> strategies.encoder(new SimpleAuthenticationEncoder());
//	}
}
