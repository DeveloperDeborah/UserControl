package no.runsafe.UserControl.database;

import no.runsafe.framework.api.database.ISchemaUpdate;
import no.runsafe.framework.api.database.Repository;
import no.runsafe.framework.api.database.SchemaUpdate;
import no.runsafe.framework.api.player.IPlayer;

import javax.annotation.Nonnull;

public class PlayerKickLog extends Repository
{
	@Nonnull
	@Override
	public String getTableName()
	{
		return "player_kick_log";
	}

	@Nonnull
	@Override
	public ISchemaUpdate getSchemaUpdateQueries()
	{
		ISchemaUpdate update = new SchemaUpdate();

		update.addQueries(
			"CREATE TABLE player_kick_log (" +
				"`name` varchar(255) NOT NULL," +
				"`timestamp` datetime NOT NULL," +
				"`kick_by` varchar(255) NOT NULL," +
				"`reason` varchar(255) NULL," +
				"`banned` bool NULL," +
				"PRIMARY KEY(`name`,`timestamp`)" +
			")"
		);

		update.addQueries(
			"ALTER TABLE `" + getTableName() + "` CHANGE `name` `player` varchar(36) NOT NULL",
			// Kicked player names -> Unique IDs
			"UPDATE IGNORE `" + getTableName() + "` SET `player` = " +
				"COALESCE((SELECT `uuid` FROM player_db WHERE `name`=`" + getTableName() + "`.`player`), `player`) " +
				"WHERE length(`player`) != 36",
			"ALTER TABLE `" + getTableName() + "` MODIFY COLUMN `kick_by` VARCHAR(36)",
			// Kicker names -> Unique IDs
			"UPDATE IGNORE `" + getTableName() + "` SET `kick_by` = " +
				"COALESCE((SELECT `uuid` FROM player_db WHERE `name`=`" + getTableName() + "`.`kick_by`), `kick_by`) " +
				"WHERE length(`kick_by`) != 36 AND `kick_by` != 'console'"
		);

		return update;
	}

	public void logKick(IPlayer kicker, IPlayer player, String reason, boolean banned)
	{
		database.update(
			"INSERT INTO player_kick_log (`player`, `timestamp`, `kick_by`, `reason`, `banned`) VALUES (?, NOW(), ?, ?, ?)",
			player, kicker == null ? "console" : kicker, reason, banned
		);
	}
}
