package no.runsafe.UserControl.database;

import no.runsafe.framework.api.database.*;
import no.runsafe.framework.api.hook.IPlayerDataProvider;
import no.runsafe.framework.api.hook.PlayerData;
import no.runsafe.framework.api.player.IPlayer;
import org.apache.commons.lang.StringUtils;
import java.time.Duration;

import javax.annotation.Nonnull;
import java.util.*;

public class PlayerSessionLog extends Repository implements IPlayerDataProvider
{
	@Nonnull
	@Override
	public String getTableName()
	{
		return "player_session";
	}

	@Nonnull
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

		update.addQueries( // Update null UUIDs.
			String.format(
				"UPDATE `" + getTableName() + "` SET `uuid` = " +
					"COALESCE((SELECT `uuid` FROM player_db WHERE `name`=`%s`.`name`), 'default') " +
					"WHERE uuid IS NULL",
				getTableName(), getTableName()
			),
			"ALTER TABLE `" + getTableName() + "` MODIFY COLUMN `uuid` VARCHAR(36) NOT NULL"
		);

		return update;
	}

	public Duration GetTimePlayed(IPlayer player)
	{
		Long time = database.queryLong(
			"SELECT SUM(TIMESTAMPDIFF(MINUTE,login,IFNULL(logout,NOW()))) AS time " +
				"FROM player_session " +
				"WHERE `uuid`=?",
			player
		);
		return time == null ? null : Duration.ofMinutes(time);
	}

	public void logSessionStart(IPlayer player)
	{
		String group = null;
		List<String> groups = player.getGroups();
		if (!groups.isEmpty())
			group = StringUtils.join(groups, ",");
		database.update(
			"INSERT INTO player_session (`name`, `ip`, `login`, `group`, `uuid`) VALUES (?, INET_ATON(?), NOW(), ?, ?)",
			player.getName(),
			player.getIP(),
			group,
			player
		);
	}

	public void logSessionClosed(IPlayer player, String quitMessage)
	{
		database.update(
			"UPDATE player_session SET logout=NOW(), quit_message=? WHERE uuid=? AND logout IS NULL",
			quitMessage, player
		);
	}

	public void closeAllSessions(String quitMessage)
	{
		database.update(
			"UPDATE player_session SET logout=NOW(), quit_message=? WHERE logout IS NULL",
			quitMessage
		);
	}

	public Map<IPlayer, List<String>> findAlternateAccounts(IPlayer player)
	{
		ISet alts = database.query(
			"SELECT DISTINCT uuid, INET_NTOA(ip) AS ip FROM player_session WHERE uuid != ? AND ip IN (SELECT DISTINCT ip FROM player_session WHERE uuid = ?)",
			player, player
		);
		Map<IPlayer, List<String>> filteredAlts = new HashMap<>();
		if (alts.isEmpty() || player.hasPermission("runsafe.usercontrol.secretAlt"))
		{
			return filteredAlts;
		}
		for (IRow alt : alts)
		{
			IPlayer altPlayer = alt.Player("uuid");
			if (altPlayer.hasPermission("runsafe.usercontrol.secretAlt"))
			{
				continue;
			}
			if (!filteredAlts.containsKey(altPlayer))
			{
				filteredAlts.put(altPlayer, new ArrayList<>());
			}
			String ip = alt.String("ip");
			filteredAlts.get(altPlayer).add(ip);
		}
		return filteredAlts;
	}

	@Override
	public void GetPlayerData(PlayerData data)
	{
		data.addData(
			"usercontrol.alts",
			() ->
			{
				Map<IPlayer, List<String>> alts = findAlternateAccounts(data.getPlayer());
				if (alts.isEmpty())
					return "none";
				List<String> altNames = new ArrayList<>();
				for (IPlayer alt : alts.keySet())
				{
					altNames.add(String.format("%s: %s", alt.getPrettyName(), formatIPList(alts.get(alt))));
				}
				return String.join(", ", altNames);
			}
		);
		if (data.getContext().hasPermission("runsafe.usercontrol.ip"))
		{
			data.addData(
				"usercontrol.ips",
				() ->
				{
					List<String> ipList = database.queryStrings(
						"SELECT DISTINCT INET_NTOA(ip) FROM player_session WHERE uuid = ?",
						data.getPlayer()
					);
					return formatIPList(ipList);
				}
			);
		}
	}

	private static String formatIPList(List<String> ipList)
	{
		return String.format("&3%s&r", String.join(" ", ipList));
	}
}
