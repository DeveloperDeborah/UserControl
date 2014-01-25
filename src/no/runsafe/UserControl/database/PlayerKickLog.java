package no.runsafe.UserControl.database;

import no.runsafe.framework.api.database.IDatabase;
import no.runsafe.framework.api.database.ISchemaUpdate;
import no.runsafe.framework.api.database.Repository;
import no.runsafe.framework.api.database.SchemaUpdate;
import no.runsafe.framework.api.player.IPlayer;

public class PlayerKickLog extends Repository
{
	@Override
	public String getTableName()
	{
		return "player_kick_log";
	}

	@Override
	public ISchemaUpdate getSchemaUpdateQueries()
	{
		ISchemaUpdate update = new SchemaUpdate();

		update.addQueries(
			"CREATE TABLE player_kick_log (" +
				"`name` varchar(255) NOT NULL," +
				"`timestamp` datetime NULL," +
				"`kick_by` varchar(255) NOT NULL," +
				"`reason` varchar(255) NULL," +
				"`banned` bool NULL," +
				"PRIMARY KEY(`name`,`timestamp`)" +
			")"
		);

		return update;
	}

	public void logKick(IPlayer kicker, IPlayer player, String reason, boolean banned)
	{
		database.update(
			"INSERT INTO player_kick_log (`name`, `timestamp`, `kick_by`, `reason`, `banned`) VALUES (?, NOW(), ?, ?, ?)",
			player.getName(), kicker == null ? "console" : kicker.getName(), reason, banned
		);
	}
}
