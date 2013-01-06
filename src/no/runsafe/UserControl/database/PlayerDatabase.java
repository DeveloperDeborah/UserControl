package no.runsafe.UserControl.database;

import no.runsafe.framework.database.IDatabase;
import no.runsafe.framework.database.ISchemaChanges;
import no.runsafe.framework.hook.IPlayerDataProvider;
import no.runsafe.framework.hook.IPlayerLookupService;
import no.runsafe.framework.output.IOutput;
import no.runsafe.framework.server.player.RunsafePlayer;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;
import org.joda.time.format.PeriodFormat;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class PlayerDatabase implements ISchemaChanges, IPlayerLookupService, IPlayerDataProvider
{
	public PlayerDatabase(IOutput console, IDatabase database)
	{
		this.console = console;
		this.database = database;
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
		PreparedStatement update = database.prepare(
			"INSERT INTO player_db (`name`,`joined`,`login`,`ip`) VALUES (?,NOW(),NOW(),INET_ATON(?))" +
				"ON DUPLICATE KEY UPDATE `login`=VALUES(`login`), `ip`=VALUES(`ip`)"
		);
		try
		{
			update.setString(1, player.getName());
			update.setString(2, player.getRawPlayer().getAddress().getAddress().getHostAddress());
			update.executeUpdate();
			console.fine("Finished updating player_db with login time");
		}
		catch (SQLException e)
		{
			console.write(e.getMessage());
		}
	}

	public void logPlayerBan(RunsafePlayer player, RunsafePlayer banner, String reason)
	{
		PreparedStatement update = database.prepare(
			"UPDATE player_db SET `banned`=NOW(), ban_reason=?, ban_by=? WHERE `name`=?"
		);
		try
		{
			update.setString(1, reason);
			if (banner == null)
				update.setString(2, "console");
			else
				update.setString(2, banner.getName());
			update.setString(3, player.getName());
			update.executeUpdate();
		}
		catch (SQLException e)
		{
			console.write(e.getMessage());
		}
	}

	public void setPlayerTemporaryBan(RunsafePlayer player, DateTime temporary)
	{
		PreparedStatement update = database.prepare("UPDATE player_db SET temp_ban=? WHERE `name`=?");
		try
		{
			update.setTimestamp(1, convert(temporary));
			update.setString(2, player.getName());
			update.executeUpdate();
		}
		catch (SQLException e)
		{
			console.write(e.getMessage());
		}
	}

	public void logPlayerUnban(RunsafePlayer player)
	{
		PreparedStatement update = database.prepare(
			"UPDATE player_db SET `banned`=NULL, ban_reason=NULL, ban_by=NULL, temp_ban=NULL WHERE `name`=?"
		);
		try
		{
			update.setString(1, player.getName());
			update.executeUpdate();
		}
		catch (SQLException e)
		{
			console.write(e.getMessage());
		}
	}

	public void logPlayerLogout(RunsafePlayer player)
	{
		console.fine("Updating player_db with logout time");
		PreparedStatement update = database.prepare(
			"UPDATE player_db SET `logout`=NOW() WHERE `name`=?"
		);
		try
		{
			update.setString(1, player.getName());
			update.executeUpdate();
			console.fine("Finished updating player_db with logout time");
		}
		catch (SQLException e)
		{
			console.write(e.getMessage());
		}
	}

	public PlayerData getData(RunsafePlayer player)
	{
		PreparedStatement select = database.prepare("SELECT * FROM player_db WHERE `name`=?");
		try
		{
			select.setString(1, player.getName());
			ResultSet result = select.executeQuery();
			if (!result.first())
				return null;

			PlayerData data = new PlayerData();
			data.setBanned(convert(result.getTimestamp("banned")));
			data.setBanner(result.getString("ban_by"));
			data.setBanReason(result.getString("ban_reason"));
			data.setJoined(convert(result.getTimestamp("joined")));
			data.setLogin(convert(result.getTimestamp("login")));
			data.setLogout(convert(result.getTimestamp("logout")));
			data.setUnban(convert(result.getTimestamp("temp_ban")));
			data.setName(result.getString("name"));
			return data;
		}
		catch (SQLException e)
		{
			console.write(e.getMessage());
			return null;
		}
	}

	private DateTime convert(Timestamp timestamp)
	{
		if (timestamp == null)
			return null;
		return new DateTime(timestamp);
	}

	private Timestamp convert(DateTime dateTime)
	{
		if (dateTime == null)
			return null;
		return new Timestamp(dateTime.getMillis());
	}

	private final IOutput console;
	private final IDatabase database;

	@Override
	public List<String> findPlayer(String lookup)
	{
		PreparedStatement select = database.prepare(
			"SELECT name FROM player_db WHERE name LIKE ?"
		);
		try
		{
			select.setString(1, String.format("%s%%", lookup));
			ResultSet hits = select.executeQuery();
			ArrayList<String> result = new ArrayList<String>();
			while (hits.next())
				result.add(hits.getString("name"));
			return result;
		}
		catch (SQLException e)
		{
			console.write(e.getMessage());
			return null;
		}
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
		result.put("usercontrol.joined",  DATE_FORMAT.print(data.getJoined()));
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

	private final PeriodType SEEN_FORMAT = PeriodType.standard().withMillisRemoved().withSecondsRemoved();
	private final DateTimeFormatter DATE_FORMAT = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm");
}
