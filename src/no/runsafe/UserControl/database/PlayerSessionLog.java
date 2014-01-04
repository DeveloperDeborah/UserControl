package no.runsafe.UserControl.database;

import no.runsafe.framework.api.database.IDatabase;
import no.runsafe.framework.api.database.Repository;
import no.runsafe.framework.api.player.IPlayer;
import org.apache.commons.lang.StringUtils;
import org.joda.time.Duration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class PlayerSessionLog extends Repository
{
	public PlayerSessionLog(IDatabase db)
	{
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
		HashMap<Integer, List<String>> queries = new LinkedHashMap<Integer, List<String>>();
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

	private final IDatabase database;
}
