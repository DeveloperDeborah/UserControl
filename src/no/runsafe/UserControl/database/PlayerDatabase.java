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
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.PeriodFormat;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.LinkedHashMap;

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

		update.addQueries("ALTER TABLE player_db RENAME TO player_db_old");

		update.addQueries( // Create new table based on player uuids instead of usernames.
			"CREATE TABLE `" + getTableName() + "` (" +
				"`uuid` varchar(255) NOT NULL," +
				"`joined` datetime NOT NULL," +
				"`login` datetime NOT NULL," +
				"`logout` datetime NULL," +
				"`banned` datetime NULL," +
				"`temp_ban` VARCHAR(255) NULL," +
				"`ban_reason` varchar(255) NULL," +
				"`ban_by` varchar(255) NULL," +
				"`ip` int unsigned NULL," +
				"PRIMARY KEY(`uuid`)" +
			")"
		);

		update.addQueries( // Migrate to new table ignoring duplicates.
			"INSERT IGNORE INTO `" + getTableName() + "` " +
				"(`uuid`, `joined`, `login`, `logout`, `banned`, `temp_ban`, `ban_reason`, `ban_by`, `ip`) " +
				"SELECT `uuid`, `joined`, `login`, `logout`, `banned`, `temp_ban`, `ban_reason`, `ban_by`, `ip` " +
				"FROM `player_db_old`"
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
		console.debugFine("Updating player_db with login time");
		database.update(
			"INSERT INTO player_db (`uuid`,`joined`,`login`,`ip`) VALUES (?,NOW(),NOW(),INET_ATON(?))" +
				"ON DUPLICATE KEY UPDATE `uuid`=VALUES(`uuid`), `login`=VALUES(`login`), `ip`=VALUES(`ip`)",
			player, player.getIP()
		);
		dataCache.Invalidate(player);
		playerUsernameLog.purgeLookupCache();
	}

	public void logPlayerBan(IPlayer player, IPlayer banner, String reason)
	{
		database.update(
			"UPDATE player_db SET `banned`=NOW(), ban_reason=?, ban_by=? WHERE `uuid`=?",
			reason, banner == null ? "console" : banner.getName(), player
		);
		dataCache.Invalidate(player);
	}

	public void setPlayerTemporaryBan(IPlayer player, DateTime temporary)
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
		data.setBanned(raw.DateTime("banned"));
		data.setBanner(raw.String("ban_by"));
		data.setBanReason(raw.String("ban_reason"));
		data.setJoined(raw.DateTime("joined"));
		data.setLogin(raw.DateTime("login"));
		data.setLogout(raw.DateTime("logout"));
		data.setUnban(raw.DateTime("temp_ban"));

		return dataCache.Cache(player, data);
	}

	@Override
	public HashMap<String, String> GetPlayerData(IPlayer player)
	{
		PlayerData data = getData(player);
		HashMap<String, String> result = new LinkedHashMap<>();
		if (data.getBanned() != null)
		{
			result.put("usercontrol.ban.status", "true");
			result.put("usercontrol.ban.timestamp", DATE_FORMAT.print(data.getBanned()));
			result.put("usercontrol.ban.reason", data.getBanReason());
			if (data.getUnban() != null)
				result.put("usercontrol.ban.temporary", DATE_FORMAT.print(data.getUnban()));
			result.put("usercontrol.ban.by", data.getBanner());
		}
		else
			result.put("usercontrol.ban.status", "false");
		result.put("usercontrol.joined", DATE_FORMAT.print(data.getJoined()));
		result.put("usercontrol.login", DATE_FORMAT.print(data.getLogin()));
		result.put("usercontrol.logout", DATE_FORMAT.print(data.getLogout()));
		result.put("usercontrol.pastNames", StringUtils.join(playerUsernameLog.getUsedUsernames(player.getUniqueId()), ", "));
		if (data.getLogout() != null && data.getLogout().isAfter(data.getLogin()))
		{
			Period period = new Period(data.getLogout(), DateTime.now(), SEEN_FORMAT);
			result.put("usercontrol.seen", PeriodFormat.getDefault().print(period));
		}
		return result;
	}

	@Override
	public DateTime GetPlayerLogout(IPlayer player)
	{
		PlayerData data = getData(player);
		if (data == null)
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
	private final PeriodType SEEN_FORMAT = PeriodType.standard().withMillisRemoved().withSecondsRemoved();
	private final DateTimeFormatter DATE_FORMAT = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm");
	private final TimedCache<IPlayer, PlayerData> dataCache;
}
