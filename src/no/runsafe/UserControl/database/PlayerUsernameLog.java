package no.runsafe.UserControl.database;

import no.runsafe.framework.api.IScheduler;
import no.runsafe.framework.api.database.ISchemaUpdate;
import no.runsafe.framework.api.database.Repository;
import no.runsafe.framework.api.database.SchemaUpdate;
import no.runsafe.framework.api.hook.IPlayerLookupService;
import no.runsafe.framework.api.player.IPlayer;
import no.runsafe.framework.timer.TimedCache;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

public class PlayerUsernameLog extends Repository implements IPlayerLookupService
{
	public PlayerUsernameLog(IScheduler scheduler)
	{
		this.lookupCache = new TimedCache<>(scheduler);
		this.uniqueIdCache = new TimedCache<>(scheduler);
		this.usernameCache = new TimedCache<>(scheduler);
	}

	@Nonnull
	@Override
	public String getTableName()
	{
		return "player_username_log";
	}

	@Nonnull
	@Override
	public ISchemaUpdate getSchemaUpdateQueries()
	{
		ISchemaUpdate update = new SchemaUpdate();

		update.addQueries(
			"CREATE TABLE `" + getTableName() + "` (" +
				"`uuid` varchar(36) NOT NULL," +
				"`name` varchar(16) NOT NULL," +
				"`last_login` datetime NOT NULL," +
				"PRIMARY KEY(`uuid`, `name`)" +
			")",
			"INSERT INTO `" + getTableName() + "` (`uuid`, `name`, `last_login`) " +
				"SELECT `uuid`, `name`, `login` from `player_db`"
		);
		update.addQueries(
			"INSERT IGNORE INTO `" + getTableName() + "` (`uuid`, `name`, `last_login`) " +
				"VALUES ('" + consoleUUID + "', 'console', '1970-01-01');"
		);

		return update;
	}

	@Nullable
	@Override
	public List<String> findPlayer(String lookup)
	{
		if (lookup == null)
			return null;

		List<String> result = lookupCache.Cache(lookup);
		if (result != null)
			return result;
		result = database.queryStrings(
			"SELECT name FROM `" + getTableName() + "` WHERE name LIKE ?",
			String.format("%s%%", SQLWildcard.matcher(lookup).replaceAll("\\\\$1"))
		);
		return lookupCache.Cache(lookup, result);
	}

	/**
	 * Gets a list of all usernames a player has logged in with.
	 *
	 * @param playerId Identifying number for the player to look up.
	 * @return List of used usernames ordered from most recently logged in first.
	 *         Null if the player has never logged into the server.
	 */
	@Nullable
	public List<String> getUsedUsernames(UUID playerId)
	{
		if (playerId == null)
			return null;

		List<String> playerNames = usernameCache.Cache(playerId);
		if (playerNames != null && !playerNames.isEmpty())
			return playerNames;

		List<String> result = database.queryStrings(
			"SELECT `name` FROM `" + getTableName() + "` WHERE `uuid` = ? ORDER BY `last_login` DESC",
			playerId.toString()
		);

		if (result.isEmpty())
			return null;

		usernameCache.Cache(playerId, result);
		return result;
	}

	/**
	 * Gets the latest username a player has logged in with.
	 *
	 * @param playerId Identifying number for the player to look up.
	 * @return The latest username a player has logged in with.
	 *         Might not be the player's current username.
	 *         Null if the player has never logged into the server.
	 */
	@Nullable
	public String getLatestUsername(UUID playerId)
	{
		List<String> playerNames = getUsedUsernames(playerId);

		if (playerNames == null)
			return null;

		return playerNames.get(0);
	}

	/**
	 * Gets the UUID of all players who have logged in with a username.
	 *
	 * @param playerName Exact name of the player to lookup.
	 *                   Not case-sensitive.
	 * @return List of UUIDs ordered from most recently logged in first.
	 *         Null if no one has logged in with that username.
	 */
	@Nullable
	public List<UUID> getUniqueIdsFromUsername(String playerName)
	{
		if (playerName == null || playerName.isEmpty() || playerName.length() > 16)
			return null;

		List<UUID> playerIds = uniqueIdCache.Cache(playerName);
		if (playerIds != null && !playerIds.isEmpty())
			return playerIds;

		List<String> result = database.queryStrings(
			"SELECT `uuid` FROM `" + getTableName() + "` WHERE `name` = ? ORDER BY `last_login` DESC",
			playerName
		);

		if (result.isEmpty())
			return null;

		playerIds = new ArrayList<>(result.size());
		for (String playerIdString : result)
		{
			playerIds.add(UUID.fromString(playerIdString));
		}

		uniqueIdCache.Cache(playerName, playerIds);
		return playerIds;
	}

	/**
	 * Looks up the Unique ID of the player who logged the most recently with a specified username.
	 *
	 * @param playerName Exact name of the player to lookup.
	 *                   Not case-sensitive.
	 * @return the UUID of the last player to log in.
	 *         Null if no one has logged in with that username.
	 */
	@Nullable
	@Override
	public UUID findPlayerUniqueId(String playerName)
	{
		List<UUID> playerIds = getUniqueIdsFromUsername(playerName);

		if (playerIds == null)
			return null;

		return playerIds.get(0);
	}

	/**
	 * Records a player's username and UUID at the current time.
	 * Updates log in time if the player's username is already stored.
	 *
	 * @param player Player that just logged in.
	 */
	public void logPlayerLogin(IPlayer player)
	{
		database.update(
			"INSERT INTO `" + getTableName() + "`(`uuid`,`name`,`last_login`) VALUES (?,?,NOW())" +
				"ON DUPLICATE KEY UPDATE `last_login`=NOW()",
			player,
			player.getName()
		);
	}

	void purgeLookupCache()
	{
		lookupCache.Purge();
	}

	public final UUID consoleUUID = UUID.fromString("00000000-0000-0000-0000-000000000000");
	private final Pattern SQLWildcard = Pattern.compile("([%_])");
	private final TimedCache<String, List<String>> lookupCache;
	private final TimedCache<String, List<UUID>> uniqueIdCache;
	private final TimedCache<UUID, List<String>> usernameCache;
}
