package org.homepoker.client;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.homepoker.client.util.CountingLogger;
import org.homepoker.client.util.DateUtils;
import org.homepoker.client.util.JsonUtils;
import org.homepoker.domain.common.ValidationException;
import org.homepoker.domain.game.CashGameDetails;
import org.homepoker.domain.game.GameCriteria;
import org.homepoker.domain.game.GameStatus;
import org.homepoker.domain.game.GameType;
import org.springframework.shell.Availability;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellMethodAvailability;
import org.springframework.shell.standard.ShellOption;

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

	@ShellMethod("Find matching cash games [name, start-date, end-date].")
    public void findCashGames(
    		@ShellOption(defaultValue = "t", help="Find all games on or after this given start date.") String startDate,
    		@ShellOption(defaultValue = ShellOption.NULL, help="Find all games on or before this given end date.") String endDate,
    		@ShellOption(defaultValue = ShellOption.NULL, help="The name of the game, supports regular expressions.") String name,
    		@ShellOption(defaultValue = ShellOption.NULL, help="The status of the game") String status) {
		
		CountingLogger<CashGameDetails> counter = new CountingLogger<>();
		GameStatus gameStatus = null;
		if (status != null) {
			gameStatus = GameStatus.valueOf(status);
		}
		LocalDate start = DateUtils.stringToDate(startDate);
		LocalDate end = DateUtils.stringToDate(endDate);
		
		connectionManager.getRsocketRequester()
			.route("admin-find-cash-games")
			.data(new GameCriteria(name, gameStatus, start, end))
			.retrieveFlux(CashGameDetails.class)
			.doOnNext(counter::logElement)
			.blockLast();
		
		log.info("Search Complete. Found [" + counter.getCount() + "] games.");
		
	}
	
	@ShellMethod("Create a new cash game. [name, start-date, start-time, buy-in-amount, buy-in-chips, small-blind].")
    public void createCashGame(String name, String startDate, String startTime, BigDecimal buyInAmount, Integer buyInChips, Integer smallBlind) {
		if (connectionManager.getCurrentUser() == null) {
			throw new ValidationException("You cannot create a game unless you are logged in.");
		}
		
		LocalDateTime startTimestamp = LocalDateTime.of(DateUtils.stringToDate(startDate), DateUtils.stringToTime(startTime));
		CashGameDetails gameDetails = CashGameDetails.builder()				
			.gameType(GameType.TEXAS_HOLDEM)
			.name(name)
			.ownerLoginId(connectionManager.getCurrentUser().getLoginId())
			.startTimestamp(startTimestamp)
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

	@ShellMethod("Update an existing cash game. [gameId, name, start-timestamp, buy-in-amount, buy-in-chips, small-blind].")
    public void updateCashGame(String gameId, String name, String startTimestamp, BigDecimal buyInAmount, Integer buyInChips, Integer smallBlind) {
		if (connectionManager.getCurrentUser() == null) {
			throw new ValidationException("You cannot create a game unless you are logged in.");
		}
		CashGameDetails gameDetails = CashGameDetails.builder()
			.id(gameId)
			.gameType(GameType.TEXAS_HOLDEM)
			.name(name)
			.ownerLoginId(connectionManager.getCurrentUser().getLoginId())
			.startTimestamp(DateUtils.stringToDateTime(startTimestamp))
			.buyInAmount(buyInAmount)
			.buyInChips(buyInChips)
			.smallBlind(smallBlind)
			.bigBlind(smallBlind*2)
			.build();
			
        CashGameDetails persistedGameDetails = connectionManager.getRsocketRequester()
            .route("admin-update-cash-game")
            .data(gameDetails)
            .retrieveMono(CashGameDetails.class)
            .block();
        
        log.info("Game has been updated:\n" + JsonUtils.toFormattedJson(persistedGameDetails));        
    }

	@ShellMethod("Delete a cash game [gameId].")
    public void deleteCashGame(String gameId) throws IOException {
		
         
        connectionManager.getRsocketRequester()
                .route("admin-delete-cash-game")
                .data(gameId)
                .retrieveMono(Void.class)
                .block();
        log.info("\nGame [{}] has been deleted.", gameId);
    }
	
	@ShellMethodAvailability({"create-cash-game", "update-cash-game"})
    private Availability validConnection() {
		return (connectionManager.getCurrentUser() == null) ?
    		Availability.unavailable("You must be logged in as a user."):
    		Availability.available();
    }	
}
