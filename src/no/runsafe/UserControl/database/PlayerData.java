package no.runsafe.UserControl.database;

import org.joda.time.DateTime;

import java.util.UUID;

public class PlayerData
{
	public DateTime getJoined()
	{
		return joined;
	}

	public void setJoined(DateTime joined)
	{
		this.joined = joined;
	}

	public DateTime getLogin()
	{
		return login;
	}

	public void setLogin(DateTime login)
	{
		this.login = login;
	}

	public DateTime getLogout()
	{
		return logout;
	}

	public void setLogout(DateTime logout)
	{
		this.logout = logout;
	}

	public DateTime getBanned()
	{
		return banned;
	}

	public void setBanned(DateTime banned)
	{
		this.banned = banned;
	}

	public DateTime getUnban()
	{
		return unban;
	}

	public void setUnban(DateTime unbanned)
	{
		unban = unbanned;
	}

	public String getBanReason()
	{
		return banReason;
	}

	public void setBanReason(String reason)
	{
		banReason = reason;
	}

	public UUID getBanningPlayerUUID()
	{
		return banningPlayerUUID;
	}

	public void setBanningPlayer(UUID uuid)
	{
		banningPlayerUUID = uuid;
	}

	private DateTime joined;
	private DateTime login;
	private DateTime logout;
	private DateTime banned;
	private UUID banningPlayerUUID;
	private DateTime unban;
	private String banReason;
}
