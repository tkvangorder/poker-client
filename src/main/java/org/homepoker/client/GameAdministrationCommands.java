package org.homepoker.client;

import java.math.BigDecimal;

import org.homepoker.client.util.DateUtils;
import org.homepoker.client.util.JsonUtils;
import org.homepoker.domain.common.ValidationException;
import org.homepoker.domain.game.CashGameDetails;
import org.homepoker.domain.game.GameType;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

import lombok.extern.slf4j.Slf4j;

/**
 * A group of client-side commands for game administration.
 * @author tyler.vangorder
 *
 */
@Slf4j
@ShellComponent
public class GameAdministrationCommands {

	private final RSocketClientConnectionManager connectionManager;

	public GameAdministrationCommands(RSocketClientConnectionManager connectionManager) {
		this.connectionManager = connectionManager;
	}
	
	@ShellMethod("Create a new cash game. [name, start-timestamp, buy-in-amount, buy-in-chips, small-blind].")
    public void createCashGame(String name, String startTimestamp, BigDecimal buyInAmount, Integer buyInChips, Integer smallBlind) {
		if (connectionManager.getCurrentUser() == null) {
			throw new ValidationException("You cannot create a game unless you are logged in.");
		}
		CashGameDetails gameDetails = CashGameDetails.builder()				
			.gameType(GameType.TEXAS_HOLDEM)
			.name(name)
			.ownerLoginId(connectionManager.getCurrentUser().getLoginId())
			.startTimestamp(DateUtils.stringToDate(startTimestamp))
			.buyInAmount(buyInAmount)
			.buyInChips(buyInChips)
			.smallBlind(smallBlind)
			.bigBlind(smallBlind*2)
			.build();
			
        CashGameDetails persistedGameDetails = connectionManager.getRsocketRequester()
            .route("admin-create-cash-game")
            .data(gameDetails)
            .retrieveMono(CashGameDetails.class)
            .block();
        
        log.info("Game has been created:\n" + JsonUtils.toFormattedJson(persistedGameDetails));
        
    }
	
}
