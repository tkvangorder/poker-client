package org.homepoker.client;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.homepoker.client.util.CountingLogger;
import org.homepoker.client.util.DateUtils;
import org.homepoker.client.util.JsonUtils;
import org.homepoker.client.util.StringUtils;
import org.homepoker.domain.common.ValidationException;
import org.homepoker.domain.game.CashGameDetails;
import org.homepoker.domain.game.GameCriteria;
import org.homepoker.domain.game.GameStatus;
import org.homepoker.domain.game.GameType;
import org.homepoker.domain.game.TournamentGameDetails;
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
	public void createCashGame(
		@ShellOption(help="A human-readable name of the game.") String name,
		@ShellOption(help="The date on which the game will take place. [yyyy-MM-dd] or use 't+7' for today plus seven days") String startDate,
		@ShellOption(help="The time at which the game will start. [HH:mm] Hours are expressed from 00-23") String startTime,
		@ShellOption(defaultValue="20.00", help="Buy-in amount in dollars.") BigDecimal buyInAmount,
		@ShellOption(defaultValue="10000", help="Number of chips given for the buy-in amount") Integer buyInChips,
		@ShellOption(defaultValue="50", help="Small Blind") Integer smallBlind) {
		
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
    public void updateCashGame(
    		@ShellOption(help="The ID assigned to the game when it was created.") String gameId,
    		@ShellOption(help="A human-readable name of the game.") String name,
    		@ShellOption(help="The date on which the game will take place. [yyyy-MM-dd] or use 't+7' for today plus seven days") String startDate,
    		@ShellOption(help="The time at which the game will start. [HH:mm] Hours are expressed from 00-23") String startTime,
    		@ShellOption(defaultValue="20.00", help="Buy-in amount in dollars.") BigDecimal buyInAmount,
    		@ShellOption(defaultValue="10000", help="Number of chips given for the buy-in amount") Integer buyInChips,
    		@ShellOption(defaultValue="50", help="Small Blind") Integer smallBlind) {

		if (connectionManager.getCurrentUser() == null) {
			throw new ValidationException("You cannot create a game unless you are logged in.");
		}

		LocalDateTime startTimestamp = LocalDateTime.of(DateUtils.stringToDate(startDate), DateUtils.stringToTime(startTime));

		CashGameDetails gameDetails = CashGameDetails.builder()
			.id(gameId)
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
			.route("admin-find-tournament-games")
			.data(new GameCriteria(name, gameStatus, start, end))
			.retrieveFlux(TournamentGameDetails.class)
			.doOnNext(counter::logElement)
			.blockLast();
		
		log.info("Search Complete. Found [" + counter.getCount() + "] games.");
	}

	@ShellMethod("Create a new tournament game. [name, start-date, start-time, number-of-rebuys, add-on-allowed, buy-in-amount, rebuy-amount, add-on-amount, buy-in-chips, rebuy-chips, add-on-chips, tournament-length-hours, cliff-level].")
    public void createTournamentGame(
    		@ShellOption(help="A human-readable name of the game.") String name,
    		@ShellOption(help="The date on which the game will take place. [yyyy-MM-dd] or use 't+7' for today plus seven days.") String startDate,
    		@ShellOption(help="The time at which the game will start. [HH:mm] Hours are expressed from 00-23") String startTime,
    		@ShellOption(help="The number of re-buys allowed.") Integer numberOfRebuys,
    		@ShellOption(help="Is an add-on allowed.") String addOnAllowed,
    		@ShellOption(defaultValue="20.00", help="Buy-in amount in dollars.") BigDecimal buyInAmount,
    		@ShellOption(defaultValue=ShellOption.NULL, help="Re-buy amount in dollars.") BigDecimal rebuyAmount,
    		@ShellOption(defaultValue=ShellOption.NULL, help="Add-on amount in dollars.") BigDecimal addOnAmount,
    		@ShellOption(defaultValue="10000", help="Number of chips given for the buy-in amount.") Integer buyInChips,
    		@ShellOption(defaultValue=ShellOption.NULL, help="Number of chips given for a re-buy.") Integer rebuyChips,
    		@ShellOption(defaultValue=ShellOption.NULL, help="Number of chips given for an add-on.") Integer addOnChips,
    		@ShellOption(defaultValue=ShellOption.NULL, help="Estimated tournament length") Integer tounrnamentLengthHours,
    		@ShellOption(defaultValue=ShellOption.NULL, help="The blind level at which rebuys are no longer allowed and add-ons are applied.") Integer cliffLevel
    		) {
		if (connectionManager.getCurrentUser() == null) {
			throw new ValidationException("You cannot create a game unless you are logged in.");
		}
		 
		LocalDateTime startTimestamp = LocalDateTime.of(DateUtils.stringToDate(startDate), DateUtils.stringToTime(startTime));
		TournamentGameDetails gameDetails = TournamentGameDetails.builder()				
			.gameType(GameType.TEXAS_HOLDEM)
			.name(name)
			.ownerLoginId(connectionManager.getCurrentUser().getLoginId())
			.startTimestamp(startTimestamp)
			.buyInAmount(buyInAmount)
			.buyInChips(buyInChips)
			.estimatedTournamentLengthHours(tounrnamentLengthHours)
			.numberOfRebuys(numberOfRebuys)
			.addOnAllowed(StringUtils.stringToboolean(addOnAllowed))
			.rebuyAmount(rebuyAmount)
			.rebuyChips(rebuyChips)
			.addOnAmount(addOnAmount)
			.addOnChips(addOnChips)
			.cliffLevel(cliffLevel)
			.build();
			
        TournamentGameDetails persistedGameDetails = connectionManager.getRsocketRequester()
            .route("admin-create-tournament-game")
            .data(gameDetails)
            .retrieveMono(TournamentGameDetails.class)
            .block();
        
        log.info("Game has been created:\n" + JsonUtils.toFormattedJson(persistedGameDetails));
    }
	
	@ShellMethod("Delete a tournament game [gameId].")
    public void deleteTournamentGame(String gameId) throws IOException {
		
         
        connectionManager.getRsocketRequester()
                .route("admin-delete-tournament-game")
                .data(gameId)
                .retrieveMono(Void.class)
                .block();
        log.info("\nGame [{}] has been deleted.", gameId);
    }

	@ShellMethodAvailability({"create-cash-game", "update-cash-game", "find-cash-games", "delete-cash-games"})
    private Availability validConnection() {
		return (connectionManager.getCurrentUser() == null) ?
    		Availability.unavailable("You must be logged in as a user."):
    		Availability.available();
    }	
}
