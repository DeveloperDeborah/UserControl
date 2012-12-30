package no.runsafe.UserControl.database;

import no.runsafe.framework.database.IDatabase;
import no.runsafe.framework.database.ISchemaChanges;
import no.runsafe.framework.hook.IPlayerLookupService;
import no.runsafe.framework.output.IOutput;
import no.runsafe.framework.server.player.RunsafePlayer;

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

	public void logPlayerUnban(RunsafePlayer player)
	{
		PreparedStatement update = database.prepare(
			"UPDATE player_db SET `banned`=NULL, ban_reason=NULL, ban_by=NULL WHERE `name`=?"
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
			data.setBanned(result.getTimestamp("banned"));
			data.setBanReason(result.getString("ban_reason"));
			data.setJoined(result.getTimestamp("joined"));
			data.setLogin(result.getTimestamp("login"));
			data.setLogout(result.getTimestamp("logout"));
			data.setName(result.getString("name"));
			return data;
		}
		catch (SQLException e)
		{
			console.write(e.getMessage());
			return null;
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
}
