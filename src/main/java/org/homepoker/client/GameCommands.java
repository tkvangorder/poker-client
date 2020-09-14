package org.homepoker.client;

import java.time.LocalDate;

import org.homepoker.client.util.CountingLogger;
import org.homepoker.client.util.DateUtils;
import org.homepoker.client.util.JsonUtils;
import org.homepoker.domain.common.ValidationException;
import org.homepoker.domain.game.CashGameDetails;
import org.homepoker.domain.game.GameCriteria;
import org.homepoker.domain.game.GameStatus;
import org.homepoker.domain.game.TournamentGameDetails;
import org.springframework.shell.Availability;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellMethodAvailability;
import org.springframework.shell.standard.ShellOption;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@ShellComponent
public class GameCommands {
	
	private final RSocketClientConnectionManager connectionManager;

	public GameCommands(RSocketClientConnectionManager connectionManager) {
		super();
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
			.route(RSocketRoutes.ROUTE_CASH_FIND_GAMES)
			.data(new GameCriteria(name, gameStatus, start, end))
			.retrieveFlux(CashGameDetails.class)
			.doOnNext(counter::logElement)
			.blockLast();
		
		log.info("Search Complete. Found [" + counter.getCount() + "] games.");		
	}

	@ShellMethod("Register for a cash game.")
    public void registerForCashGame(String gameId) {

		if (connectionManager.getCurrentUser() == null) {
			throw new ValidationException("You cannot register for a game unless you are logged in.");
		}
		
		CashGameDetails gameDetails = connectionManager.getRsocketRequester()
			.route(RSocketRoutes.ROUTE_CASH_REGISTER_FOR_GAME)
			.data(gameId)
			.retrieveMono(CashGameDetails.class)
			.block();
		
        log.info("You have registered for the game. :\n" + JsonUtils.toFormattedJson(gameDetails));
	}

	@ShellMethod("Find matching tournament games [start-date, end-date, name, status].")
    public void findTournamentGames(
    		@ShellOption(defaultValue = "t", help="Find all games on or after this given start date.") String startDate,
    		@ShellOption(defaultValue = ShellOption.NULL, help="Find all games on or before this given end date.") String endDate,
    		@ShellOption(defaultValue = ShellOption.NULL, help="The name of the game, supports regular expressions.") String name,
    		@ShellOption(defaultValue = ShellOption.NULL, help="The status of the game") String status) {
		
		CountingLogger<TournamentGameDetails> counter = new CountingLogger<>();
		GameStatus gameStatus = null;
		if (status != null) {
			gameStatus = GameStatus.valueOf(status);
		}
		LocalDate start = DateUtils.stringToDate(startDate);
		LocalDate end = DateUtils.stringToDate(endDate);
		
		connectionManager.getRsocketRequester()
			.route(RSocketRoutes.ROUTE_TOURNAMENT_FIND_GAMES)
			.data(new GameCriteria(name, gameStatus, start, end))
			.retrieveFlux(TournamentGameDetails.class)
			.doOnNext(counter::logElement)
			.blockLast();
		
		log.info("Search Complete. Found [" + counter.getCount() + "] games.");
	}
	
	@ShellMethod("Register for a tournament.")
    public void registerForTournament(String gameId) {

		if (connectionManager.getCurrentUser() == null) {
			throw new ValidationException("You cannot register for a game unless you are logged in.");
		}
		
		TournamentGameDetails gameDetails = connectionManager.getRsocketRequester()
			.route(RSocketRoutes.ROUTE_TOURNAMENT_REGISTER_FOR_GAME)
			.data(gameId)
			.retrieveMono(TournamentGameDetails.class)
			.block();
		
        log.info("You have registered for the game. :\n" + JsonUtils.toFormattedJson(gameDetails));
	}
	
	@ShellMethodAvailability({"find-cash-games", "find-tournament-games", "register-for-tournament", "register-for-cash-game"})
    private Availability validConnection() {
		return (connectionManager.getCurrentUser() == null) ?
    		Availability.unavailable("You must be logged in as a user."):
    		Availability.available();
    }	
}
