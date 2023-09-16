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

	public UUID getBannerUUID()
	{
		return banner_uuid;
	}

	public void setBanner(UUID uuid)
	{
		banner_uuid = uuid;
	}

	private DateTime joined;
	private DateTime login;
	private DateTime logout;
	private DateTime banned;
	private UUID banner_uuid;
	private DateTime unban;
	private String banReason;
}
