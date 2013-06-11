package no.runsafe.UserControl.database;

import no.runsafe.framework.api.database.IDatabase;
import no.runsafe.framework.api.database.Repository;
import no.runsafe.framework.minecraft.player.RunsafePlayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PlayerKickLog extends Repository
{
	public PlayerKickLog(IDatabase db)
	{
		database = db;
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
		database.Update(
			"INSERT INTO player_kick_log (`name`, `timestamp`, `kick_by`, `reason`, `banned`) VALUES (?, NOW(), ?, ?, ?)",
			player.getName(), kicker == null ? "console" : kicker.getName(), reason, banned
		);
	}

	private final IDatabase database;
}
