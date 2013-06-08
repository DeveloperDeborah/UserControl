package no.runsafe.UserControl.database;

import no.runsafe.framework.database.IDatabase;
import no.runsafe.framework.database.Repository;
import no.runsafe.framework.hook.IPlayerDataProvider;
import no.runsafe.framework.hook.IPlayerLookupService;
import no.runsafe.framework.output.IOutput;
import no.runsafe.framework.server.player.RunsafePlayer;
import no.runsafe.framework.timer.IScheduler;
import no.runsafe.framework.timer.TimedCache;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.PeriodFormat;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

public class PlayerDatabase extends Repository implements IPlayerLookupService, IPlayerDataProvider
{
	public PlayerDatabase(IOutput console, IDatabase database, IScheduler scheduler)
	{
		this.console = console;
		this.database = database;
		this.lookupCache = new TimedCache<String, List<String>>(scheduler);
	}

	@Override
	public String getTableName()
	{
		return "player_db";
	}

	@Override
	public HashMap<Integer, List<String>> getSchemaUpdateQueries()
	{
		HashMap<Integer, List<String>> queries = new HashMap<Integer, List<String>>();
		ArrayList<String> sql = new ArrayList<String>();
		sql.add(
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
		queries.put(1, sql);
		sql = new ArrayList<String>();
		sql.add("ALTER TABLE player_db ADD COLUMN temp_ban datetime NULL");
		queries.put(2, sql);
		return queries;
	}

	public void logPlayerInfo(RunsafePlayer player)
	{
		console.fine("Updating player_db with login time");
		database.Update(
			"INSERT INTO player_db (`name`,`joined`,`login`,`ip`) VALUES (?,NOW(),NOW(),INET_ATON(?))" +
				"ON DUPLICATE KEY UPDATE `name`=VALUES(`name`), `login`=VALUES(`login`), `ip`=VALUES(`ip`)",
			player.getName(), player.getRawPlayer().getAddress().getAddress().getHostAddress()
		);
	}

	public void logPlayerBan(RunsafePlayer player, RunsafePlayer banner, String reason)
	{
		database.Update(
			"UPDATE player_db SET `banned`=NOW(), ban_reason=?, ban_by=? WHERE `name`=?",
			reason, banner == null ? "console" : banner.getName(), player.getName()
		);
	}

	public void setPlayerTemporaryBan(RunsafePlayer player, DateTime temporary)
	{
		database.Update(
			"UPDATE player_db SET temp_ban=? WHERE `name`=?",
			convert(temporary), player.getName()
		);
	}

	public void logPlayerUnban(RunsafePlayer player)
	{
		database.Update(
			"UPDATE player_db SET `banned`=NULL, ban_reason=NULL, ban_by=NULL, temp_ban=NULL WHERE `name`=?",
			player.getName()
		);
	}

	public void logPlayerLogout(RunsafePlayer player)
	{
		database.Update(
			"UPDATE player_db SET `logout`=NOW() WHERE `name`=?",
			player.getName()
		);
	}

	public PlayerData getData(RunsafePlayer player)
	{
		Map<String, Object> raw = database.QueryRow("SELECT * FROM player_db WHERE `name`=?", player.getName());
		if (raw == null)
			return null;
		PlayerData data = new PlayerData();
		data.setBanned(convert(raw.get("banned")));
		data.setBanner((String) raw.get("ban_by"));
		data.setBanReason((String) raw.get("ban_reason"));
		data.setJoined(convert(raw.get("joined")));
		data.setLogin(convert(raw.get("login")));
		data.setLogout(convert(raw.get("logout")));
		data.setUnban(convert(raw.get("temp_ban")));
		data.setName((String) raw.get("name"));
		return data;
	}

	@Override
	public List<String> findPlayer(String lookup)
	{
		if (lookup == null)
			return null;

		List<String> result = lookupCache.Cache(lookup);
		if(result != null)
			return result;
		List<Object> hits = database.QueryColumn(
			"SELECT name FROM player_db WHERE name LIKE ?",
			String.format("%s%%", SQLWildcard.matcher(lookup).replaceAll("\\\\$1"))
		);
		if (hits == null)
			return null;
		result = new ArrayList<String>();
		for (Object hit : hits)
			result.add((String) hit);
		return lookupCache.Cache(lookup, result);
	}

	@Override
	public HashMap<String, String> GetPlayerData(RunsafePlayer player)
	{
		PlayerData data = getData(player);
		HashMap<String, String> result = new LinkedHashMap<String, String>();
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
		if (data.getLogout() != null && data.getLogout().isAfter(data.getLogin()))
		{
			Period period = new Period(data.getLogout(), DateTime.now(), SEEN_FORMAT);
			result.put("usercontrol.seen", PeriodFormat.getDefault().print(period));
		}
		return result;
	}

	@Override
	public DateTime GetPlayerLogout(RunsafePlayer player)
	{
		PlayerData data = getData(player);
		if (data == null)
			return null;
		return data.getLogout();
	}

	@Override
	public String GetPlayerBanReason(RunsafePlayer player)
	{
		return getData(player).getBanReason();
	}

	private final IOutput console;
	private final IDatabase database;
	private final PeriodType SEEN_FORMAT = PeriodType.standard().withMillisRemoved().withSecondsRemoved();
	private final DateTimeFormatter DATE_FORMAT = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm");
	private final Pattern SQLWildcard = Pattern.compile("([%_])");
	private final TimedCache<String, List<String>> lookupCache;
}
