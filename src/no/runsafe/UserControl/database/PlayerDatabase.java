package no.runsafe.UserControl.database;

import no.runsafe.framework.database.IDatabase;
import no.runsafe.framework.database.IRepository;
import no.runsafe.framework.database.ISchemaChanges;
import no.runsafe.framework.event.IPluginEnabled;
import no.runsafe.framework.event.player.IPlayerJoinEvent;
import no.runsafe.framework.event.player.IPlayerQuitEvent;
import no.runsafe.framework.hook.IPlayerLookupService;
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

public class PlayerDatabase implements ISchemaChanges, IPlayerLookupService
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
			while(hits.next())
				result.add(hits.getString("name"));
			return result;
		}
		catch (SQLException e)
		{
			console.write(e.getMessage());
			return null;
		}
	}
}
