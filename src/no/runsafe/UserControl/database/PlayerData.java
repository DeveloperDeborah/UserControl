package no.runsafe.UserControl.database;

import no.runsafe.framework.database.RunsafeEntity;
import org.joda.time.DateTime;

public class PlayerData
{
	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

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

	public String getBanner()
	{
		return banner;
	}

	public void setBanner(String name)
	{
		banner = name;
	}

	private String name;
	private DateTime joined;
	private DateTime login;
	private DateTime logout;
	private DateTime banned;
	private String banner;
	private DateTime unban;
	private String banReason;
}
