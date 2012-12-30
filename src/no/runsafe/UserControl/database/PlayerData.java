package no.runsafe.UserControl.database;

import no.runsafe.framework.database.RunsafeEntity;

import java.sql.Date;
import java.sql.Timestamp;

public class PlayerData extends RunsafeEntity
{
	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public Timestamp getJoined()
	{
		return joined;
	}

	public void setJoined(Timestamp joined)
	{
		this.joined = joined;
	}

	public Timestamp getLogin()
	{
		return login;
	}

	public void setLogin(Timestamp login)
	{
		this.login = login;
	}

	public Timestamp getLogout()
	{
		return logout;
	}

	public void setLogout(Timestamp logout)
	{
		this.logout = logout;
	}

	public Timestamp getBanned()
	{
		return banned;
	}

	public void setBanned(Timestamp banned)
	{
		this.banned = banned;
	}

	public String getBanReason()
	{
		return banReason;
	}

	public void setBanReason(String reason)
	{
		banReason = reason;
	}

	private String name;
	private Timestamp joined;
	private Timestamp login;
	private Timestamp logout;
	private Timestamp banned;
	private String banReason;
}
