package no.runsafe.UserControl.database;

import no.runsafe.framework.api.database.ISchemaUpdate;
import no.runsafe.framework.api.database.Repository;
import no.runsafe.framework.api.database.SchemaUpdate;
import no.runsafe.framework.api.event.IServerReady;
import no.runsafe.framework.api.player.IPlayer;
import org.apache.commons.lang.StringUtils;
import org.joda.time.Duration;

import java.util.List;

public class PlayerSessionLog extends Repository implements IServerReady
{
	@Override
	public String getTableName()
	{
		return "player_session";
	}

	@Override
	public ISchemaUpdate getSchemaUpdateQueries()
	{
		ISchemaUpdate update = new SchemaUpdate();

		update.addQueries(
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
		update.addQueries("ALTER TABLE player_session ADD COLUMN `uuid` VARCHAR(255) NULL");
		return update;
	}

	@Override
	public void OnServerReady()
	{
		database.execute(
			"UPDATE player_session " +
				"SET `uuid`=(SELECT `uuid` FROM player_db WHERE `name`=`player_session`.`name`) " +
				"WHERE uuid IS NULL"
		);
	}

	public Duration GetTimePlayed(IPlayer player)
	{
		Long time = database.queryLong(
			"SELECT SUM(TIMESTAMPDIFF(MINUTE,login,IFNULL(logout,NOW()))) AS time " +
				"FROM player_session " +
				"WHERE `name`=?",
			player.getName()
		);
		return time == null ? null : Duration.standardMinutes(time);
	}

	public void logSessionStart(IPlayer player)
	{
		String group = null;
		List<String> groups = player.getGroups();
		if (groups.size() > 0)
			group = StringUtils.join(groups, ",");
		database.update(
			"INSERT INTO player_session (`name`, `ip`, `login`, `group`) VALUES (?, INET_ATON(?), NOW(), ?)",
			player.getName(),
			player.getIP(),
			group
		);
	}

	public void logSessionClosed(IPlayer player, String quitMessage)
	{
		database.update(
			"UPDATE player_session SET logout=NOW(), quit_message=? WHERE name=? AND logout IS NULL",
			quitMessage, player.getName()
		);
	}

	public void closeAllSessions(String quitMessage)
	{
		database.update(
			"UPDATE player_session SET logout=NOW(), quit_message=? WHERE logout IS NULL",
			quitMessage
		);
	}
}
