package no.runsafe.UserControl.database;

import no.runsafe.framework.api.IScheduler;
import no.runsafe.framework.api.database.IRow;
import no.runsafe.framework.api.database.ISchemaUpdate;
import no.runsafe.framework.api.database.Repository;
import no.runsafe.framework.api.database.SchemaUpdate;
import no.runsafe.framework.api.event.IServerReady;
import no.runsafe.framework.api.hook.IPlayerDataProvider;
import no.runsafe.framework.api.hook.IPlayerSessionDataProvider;
import no.runsafe.framework.api.log.IConsole;
import no.runsafe.framework.api.log.IDebug;
import no.runsafe.framework.api.player.IPlayer;
import no.runsafe.framework.timer.TimedCache;
import no.runsafe.framework.tools.TimeFormatter;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Nonnull;
import java.time.Instant;
import java.util.*;

public class PlayerDatabase extends Repository
	implements IPlayerDataProvider, IPlayerSessionDataProvider, IServerReady
{
	public PlayerDatabase(IDebug console, IScheduler scheduler, IConsole output, PlayerUsernameLog playerUsernameLog)
	{
		this.console = console;
		this.output = output;
		this.dataCache = new TimedCache<>(scheduler);
		this.playerUsernameLog = playerUsernameLog;
	}

	@Nonnull
	@Override
	public String getTableName()
	{
		return "player_db";
	}

	@Nonnull
	@Override
	public ISchemaUpdate getSchemaUpdateQueries()
	{
		ISchemaUpdate update = new SchemaUpdate();

		update.addQueries(
			"CREATE TABLE player_db (" +
				"`name` varchar(255) NOT NULL," +
				"`joined` datetime NOT NULL," +
				"`login` datetime NOT NULL," +
				"`logout` datetime NULL," +
				"`banned` datetime NULL," +
				"`ban_reason` varchar(255) NULL," +
				"`ban_by` varchar(255) NULL," +
				"`ip` int unsigned NULL," +
				"PRIMARY KEY(`name`)" +
			")"
		);

		update.addQueries("ALTER TABLE player_db ADD COLUMN temp_ban datetime NULL");
		update.addQueries("ALTER TABLE player_db ADD COLUMN uuid VARCHAR(255) NULL");

		update.addQueries(
			// Add console
			"INSERT IGNORE INTO `" + getTableName() + "` " +
				"(`name`, `joined`, `login`, `logout`, `banned`, `ban_reason`, `ban_by`, `ip`, `temp_ban`, `uuid`) " +
				"VALUES ('console', '1970-01-01', '1970-01-01', '1970-01-01', NULL, NULL, NULL, NULL, NULL, '" + playerUsernameLog.consoleUUID +"');",

			// Add column for banningPlayer UUID
			"ALTER TABLE player_db ADD COLUMN ban_by_uuid VARCHAR(36) NULL;",
			// Convert banning console to UUID
			"UPDATE IGNORE `player_db` SET `ban_by_uuid` = '" + playerUsernameLog.consoleUUID + "' " +
				"WHERE `ban_by` = 'console' OR (`banned` IS NOT NULL AND `ban_by` IS NULL);",
			// Convert banningPlayer usernames to UUIDs
			"UPDATE IGNORE `player_db` SET `ban_by_uuid` = " +
				"(SELECT `uuid` FROM `player_username_log` WHERE `name`=`player_db`.`ban_by` LIMIT 1) " +
				"WHERE `ban_by` IS NOT NULL;",

			"ALTER TABLE player_db RENAME TO player_db_old;",
			// Create new table based on player uuids instead of usernames.
			"CREATE TABLE `" + getTableName() + "` (" +
				"`uuid` varchar(36) NOT NULL," +
				"`name` varchar(36) NOT NULL," +
				"`joined` datetime NOT NULL," +
				"`login` datetime NOT NULL," +
				"`logout` datetime NULL," +
				"`banned` datetime NULL," +
				"`temp_ban` VARCHAR(255) NULL," +
				"`ban_reason` varchar(255) NULL," +
				"`ban_by` varchar(36) NULL," +
				"`ip` int unsigned NULL," +
				"PRIMARY KEY(`uuid`)" +
			");",
			// Migrate to new table ignoring duplicates.
			"INSERT IGNORE INTO `" + getTableName() + "` " +
				"(`uuid`, `name`, `joined`, `login`, `logout`, `banned`, `temp_ban`, `ban_reason`, `ban_by`, `ip`) " +
				"SELECT `uuid`, `name`, `joined`, `login`, `logout`, `banned`, `temp_ban`, `ban_reason`, `ban_by_uuid`, `ip` " +
				"FROM `player_db_old` WHERE `uuid` IS NOT NULL;"
		);

		return update;
	}

	@Override
	public void OnServerReady()
	{
		// Clean up invalid state after a server failure
		database.update("UPDATE player_db SET `logout`=NOW() WHERE logout < login");
	}

	public void logPlayerInfo(IPlayer player)
	{
		if (player == null)
		{
			output.logError("Attempted to log player info for NULL player");
			return;
		}
		console.debugFine("Updating player_db with login time");
		console.debugFine("Player %s is named %s and has ip %s", player.getUniqueId(), player.getName(), player.getIP());
		if (database == null)
		{
			output.logError("No database, time for panic!");
			return;
		}
		database.update(
			"INSERT INTO player_db (`uuid`,`name`,`joined`,`login`,`ip`) VALUES (?,?,NOW(),NOW(),INET_ATON(?))" +
				"ON DUPLICATE KEY UPDATE `uuid`=VALUES(`uuid`), `name`=VALUES(`name`), `login`=VALUES(`login`), `ip`=VALUES(`ip`)",
			player, player.getName(), player.getIP()
		);
		dataCache.Invalidate(player);
		playerUsernameLog.purgeLookupCache();
	}

	public void logPlayerBan(IPlayer player, IPlayer banningPlayer, String reason)
	{
		database.update(
			"UPDATE player_db SET `banned`=NOW(), ban_reason=?, ban_by=? WHERE `uuid`=?",
			reason, banningPlayer == null ? playerUsernameLog.consoleUUID.toString() : banningPlayer.getUniqueId().toString(), player
		);
		dataCache.Invalidate(player);
	}

	public void setPlayerTemporaryBan(IPlayer player, Instant temporary)
	{
		database.update("UPDATE player_db SET temp_ban=? WHERE `uuid`=?", temporary, player);
		dataCache.Invalidate(player);
	}

	public void logPlayerUnban(IPlayer player)
	{
		database.update(
			"UPDATE player_db SET `banned`=NULL, ban_reason=NULL, ban_by=NULL, temp_ban=NULL WHERE `uuid`=?",
			player
		);
		dataCache.Invalidate(player);
	}

	public void logPlayerLogout(IPlayer player)
	{
		database.update(
			"UPDATE player_db SET `logout`=NOW() WHERE `uuid`=?",
			player
		);
		dataCache.Invalidate(player);
	}

	public PlayerData getData(IPlayer player)
	{
		PlayerData data = dataCache.Cache(player);
		if (data != null)
			return data;

		IRow raw = database.queryRow("SELECT * FROM player_db WHERE `uuid`=?", player.getUniqueId().toString());
		if (raw.isEmpty())
			output.logInformation("New player %s with UUID %s discovered!", player.getName(), player.getUniqueId().toString());
		else if (!player.getUniqueId().toString().equalsIgnoreCase(raw.String("uuid")))
			output.logInformation("Player %s with UUID %s changed their username!", player.getName(), raw.String("uuid"));
		data = new PlayerData();
		data.setBanned(raw.Instant("banned"));
		if (raw.String("ban_by") != null)
			data.setBanningPlayer(UUID.fromString(raw.String("ban_by")));
		else
			data.setBanningPlayer(null);
		data.setBanReason(raw.String("ban_reason"));
		data.setJoined(raw.Instant("joined"));
		data.setLogin(raw.Instant("login"));
		data.setLogout(raw.Instant("logout"));
		data.setUnban(raw.Instant("temp_ban"));

		return dataCache.Cache(player, data);
	}

	@Override
	public void GetPlayerData(no.runsafe.framework.api.hook.PlayerData data)
	{
		PlayerData playerData = getData(data.getPlayer());
		data.addData("usercontrol.ban.status", () -> playerData.getBanned() == null ? "false" : "true");
		if (playerData.getBanned() != null)
		{
			data.addData("usercontrol.ban.timestamp", () -> TimeFormatter.formatDate(playerData.getBanned()));
			data.addData("usercontrol.ban.reason", playerData.getBanReason());
			if (playerData.getUnban() != null)
				data.addData("usercontrol.ban.temporary", () -> TimeFormatter.formatDate(playerData.getUnban()));
			data.addData("usercontrol.ban.by", () -> playerUsernameLog.getLatestUsername(playerData.getBanningPlayerUUID()));
		}
		data.addData("usercontrol.joined", () -> TimeFormatter.formatDate(playerData.getJoined()));
		data.addData("usercontrol.login", () -> TimeFormatter.formatDate(playerData.getLogin()));
		data.addData("usercontrol.logout", () -> TimeFormatter.formatDate(playerData.getLogout()));
		data.addData("usercontrol.pastNames", () -> StringUtils.join(playerUsernameLog.getUsedUsernames(data.getPlayer().getUniqueId()), ", "));
		data.addData(
			"usercontrol.seen",
			() -> playerData.getLogout() == null || !playerData.getLogout().isAfter(playerData.getLogin())
				? null
				: TimeFormatter.formatInstant(playerData.getLogout())
		);
	}

	@Override
	public Instant GetPlayerLogout(IPlayer player)
	{
		if (player == null)
			return null;

		PlayerData data = getData(player);
		if (data == null || data.getLogout() == null)
			return null;

		return data.getLogout();
	}

	@Override
	public String GetPlayerBanReason(IPlayer player)
	{
		return getData(player).getBanReason();
	}

	@Override
	public boolean IsFirstSession(IPlayer player)
	{
		return GetPlayerLogout(player) == null;
	}

	private final IConsole output;
	private final IDebug console;
	private final PlayerUsernameLog playerUsernameLog;
	private final TimedCache<IPlayer, PlayerData> dataCache;
}
