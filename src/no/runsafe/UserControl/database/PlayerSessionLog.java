package no.runsafe.UserControl.database;

import no.runsafe.framework.database.IDatabase;
import no.runsafe.framework.database.ISchemaChanges;
import no.runsafe.framework.output.IOutput;
import no.runsafe.framework.server.player.RunsafePlayer;
import org.apache.commons.lang.StringUtils;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PlayerSessionLog implements ISchemaChanges
{
	public PlayerSessionLog(IOutput output, IDatabase db)
	{
		console = output;
		database = db;
	}

	@Override
	public String getTableName()
	{
		return "player_session";
	}

	@Override
	public HashMap<Integer, List<String>> getSchemaUpdateQueries()
	{
		HashMap<Integer, List<String>> queries = new HashMap<Integer, List<String>>();
		ArrayList<String> sql = new ArrayList<String>();
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
		queries.put(1, sql);
		return queries;
	}

	public void logSessionStart(RunsafePlayer player)
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

	public void logSessionClosed(RunsafePlayer player, String quitMessage)
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

	public void closeAllSessions(String quitMessage)
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
