package no.runsafe.UserControl.database;

import no.runsafe.framework.database.IDatabase;
import no.runsafe.framework.database.ISchemaChanges;
import no.runsafe.framework.output.IOutput;
import no.runsafe.framework.server.player.RunsafePlayer;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PlayerKickLog implements ISchemaChanges
{
	public PlayerKickLog(IDatabase db, IOutput output)
	{
		database = db;
		console = output;
	}

	@Override
	public String getTableName()
	{
		return "player_kick_log";
	}

	@Override
	public HashMap<Integer, List<String>> getSchemaUpdateQueries()
	{
		HashMap<Integer, List<String>> queries = new HashMap<Integer, List<String>>();
		ArrayList<String> sql = new ArrayList<String>();
		sql.add(
			"CREATE TABLE player_kick_log (" +
				"`name` varchar(255) NOT NULL," +
				"`timestamp` datetime NULL," +
				"`kick_by` varchar(255) NOT NULL," +
				"`reason` varchar(255) NULL," +
				"`banned` bool NULL," +
				"PRIMARY KEY(`name`,`timestamp`)" +
				")"
		);
		queries.put(1, sql);
		return queries;
	}

	public void logKick(RunsafePlayer kicker, RunsafePlayer player, String reason, boolean banned)
	{
		PreparedStatement logEntry = database.prepare(
			"INSERT INTO player_kick_log (`name`, `timestamp`, `kick_by`, `reason`, `banned`) VALUES (?, NOW(), ?, ?, ?)"
		);
		try
		{
			logEntry.setString(1, player.getName());
			if (kicker == null)
				logEntry.setString(2, "console");
			else
				logEntry.setString(2, kicker.getName());
			logEntry.setString(3, reason);
			logEntry.setBoolean(4, banned);
			logEntry.execute();
		}
		catch (SQLException e)
		{
			console.write(e.getMessage());
		}
	}

	private final IDatabase database;
	private final IOutput console;
}
