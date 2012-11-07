package no.runsafe.UserControl.database;

import no.runsafe.framework.database.IDatabase;
import no.runsafe.framework.database.IRepository;
import no.runsafe.framework.database.ISchemaChanges;
import no.runsafe.framework.event.IPluginEnabled;
import no.runsafe.framework.event.player.IPlayerJoinEvent;
import no.runsafe.framework.event.player.IPlayerQuitEvent;
import no.runsafe.framework.output.IOutput;
import no.runsafe.framework.server.RunsafeServer;
import no.runsafe.framework.server.event.player.RunsafePlayerJoinEvent;
import no.runsafe.framework.server.event.player.RunsafePlayerQuitEvent;
import no.runsafe.framework.server.player.RunsafePlayer;
import org.apache.commons.lang.StringUtils;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PlayerDatabase implements
	IPluginEnabled, ISchemaChanges, IPlayerJoinEvent, IPlayerQuitEvent, IRepository<PlayerData, String>
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
		sql.add(
			"CREATE TABLE player_session (" +
				"`name` varchar(255) NOT NULL," +
				"`ip` int unsigned NOT NULL," +
				"`login` datetime NOT NULL," +
				"`logout` datetime NULL," +
				"`quit_message` varchar(255) NULL," +
				"`group` varchar(255) NULL," +
				"PRIMARY KEY(`name`,`login`)" +
				")"
		);
		queries.put(2, sql);
		return queries;
	}

	@Override
	public void OnPlayerJoinEvent(RunsafePlayerJoinEvent event)
	{
		logPlayerInfo(event.getPlayer());
		logSessionStart(event.getPlayer());
	}

	@Override
	public void OnPlayerQuit(RunsafePlayerQuitEvent event)
	{
		logPlayerLogout(event.getPlayer());
		logSessionClosed(event.getPlayer(), event.getQuitMessage());
	}

	@Override
	public void OnPluginEnabled()
	{
		closeAllSessions("Possible crash");
		for(RunsafePlayer player : RunsafeServer.Instance.getOnlinePlayers())
		{
			logPlayerInfo(player);
			logSessionStart(player);
		}
	}

	@Override
	public PlayerData get(String player)
	{
		PlayerData result = null;
		PreparedStatement select = database.prepare(
			"SELECT name, joined, login, logout, banned FROM player_db WHERE name=?"
		);
		try
		{
			select.setString(1, player);
			ResultSet data = select.executeQuery();
			if (data.first())
			{
				result = new PlayerData();
				result.setName(data.getString(1));
				result.setJoined(data.getDate(2));
				result.setLogin(data.getDate(3));
				result.setLogout(data.getDate(4));
				result.setBanned(data.getDate(5));
			}
		}
		catch (SQLException e)
		{
			console.write(e.getMessage());
		}
		return result;
	}

	@Override
	public void persist(PlayerData data)
	{
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public void delete(PlayerData data)
	{
		throw new UnsupportedOperationException("Not implemented");
	}

	private void logPlayerInfo(RunsafePlayer player)
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

	private void logSessionStart(RunsafePlayer player)
	{
		PreparedStatement logSession = database.prepare(
			"INSERT INTO player_session (`name`, `ip`, `login`, `group`) VALUES (?, INET_ATON(?), NOW(), ?)"
		);
		try
		{
			logSession.setString(1, player.getName());
			logSession.setString(2, player.getRawPlayer().getAddress().getAddress().getHostAddress());
			List<String> groups = player.getGroups();
			if (groups.size() == 0)
				logSession.setString(3, null);
			logSession.setString(3, StringUtils.join(groups, ","));
			logSession.execute();
		}
		catch (SQLException e)
		{
			console.write(e.getMessage());
		}
	}

	private void logPlayerLogout(RunsafePlayer player)
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

	private void logSessionClosed(RunsafePlayer player, String quitMessage)
	{
		PreparedStatement closeSession = database.prepare(
			"UPDATE player_session SET logout=NOW(), quit_message=? WHERE name=? AND logout IS NULL"
		);
		try
		{
			closeSession.setString(1, quitMessage);
			closeSession.setString(2, player.getName());
			closeSession.executeUpdate();
		}
		catch (SQLException e)
		{
			console.write(e.getMessage());
		}
	}

	private void closeAllSessions(String quitMessage)
	{
		PreparedStatement closeSession = database.prepare(
			"UPDATE player_session SET logout=NOW(), quit_message=? WHERE logout IS NULL"
		);
		try
		{
			closeSession.setString(1, quitMessage);
			closeSession.executeUpdate();
		}
		catch (SQLException e)
		{
			console.write(e.getMessage());
		}
	}

	private final IOutput console;
	private final IDatabase database;
}
