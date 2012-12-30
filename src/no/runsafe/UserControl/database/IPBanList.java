package no.runsafe.UserControl.database;

import no.runsafe.framework.database.IDatabase;
import no.runsafe.framework.database.ISchemaChanges;
import no.runsafe.framework.output.IOutput;
import no.runsafe.framework.server.player.RunsafePlayer;
import org.apache.commons.lang.StringUtils;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class IPBanList implements ISchemaChanges
{
	public IPBanList(IDatabase db, IOutput output)
	{
		database = db;
		console = output;
	}

	@Override
	public String getTableName()
	{
		return "banned_ip_addresses";
	}

	@Override
	public HashMap<Integer, List<String>> getSchemaUpdateQueries()
	{
		HashMap<Integer, List<String>> queries = new HashMap<Integer, List<String>>();
		ArrayList<String> sql = new ArrayList<String>();
		sql.add(
			"CREATE TABLE banned_ip_addresses (" +
				"`ip` int unsigned NULL," +
				"`banned` datetime NULL," +
				"`ban_reason` varchar(255) NULL," +
				"`ban_by` varchar(255) NULL," +
				"PRIMARY KEY(`ip`)" +
				")"
		);
		queries.put(1, sql);
		return queries;
	}

	public void banIp(String ipAddress, RunsafePlayer banner, String reason)
	{
		PreparedStatement logIPBan = database.prepare(
			"INSERT INTO banned_ip_addresses (`ip`, `banned`, `ban_reason`, `ban_by`) VALUES (INET_ATON(?), NOW(), ?, ?)"
		);
		try
		{
			logIPBan.setString(1, ipAddress);
			logIPBan.setString(2, reason);
			logIPBan.setString(3, banner.getName());
			logIPBan.execute();
		}
		catch (SQLException e)
		{
			console.write(e.getMessage());
		}
	}

	public void unBanIp(String ip)
	{
		PreparedStatement removeIPBan = database.prepare(
			"DELETE FROM banned_ip_addresses WHERE `ip`=INET_ATON(?)"
		);
		try
		{
			removeIPBan.setString(1, ip);
			removeIPBan.execute();
		}
		catch (SQLException e)
		{
			console.write(e.getMessage());
		}
	}

	public String getIpBan(RunsafePlayer player)
	{
		String ipAddress = player.getRawPlayer().getAddress().getAddress().getHostAddress();
		PreparedStatement checkIPBan = database.prepare(
			"SELECT ban_reason FROM banned_ip_addresses WHERE `ip`=INET_ATON(?)"
		);
		try
		{
			checkIPBan.setString(1, ipAddress);
			ResultSet result = checkIPBan.executeQuery();
			if(!result.first())
				return null;

			return result.getString(1);
		}
		catch (SQLException e)
		{
			console.write(e.getMessage());
			return null;
		}
	}

	IDatabase database;
	IOutput console;
}
