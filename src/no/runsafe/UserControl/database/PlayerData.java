package no.runsafe.UserControl.database;

import java.time.Instant;
import java.util.UUID;

public class PlayerData
{
	public Instant getJoined()
	{
		return joined;
	}

	public void setJoined(Instant joined)
	{
		this.joined = joined;
	}

	public Instant getLogin()
	{
		return login;
	}

	public void setLogin(Instant login)
	{
		this.login = login;
	}

	public Instant getLogout()
	{
		return logout;
	}

	public void setLogout(Instant logout)
	{
		this.logout = logout;
	}

	public Instant getBanned()
	{
		return banned;
	}

	public void setBanned(Instant banned)
	{
		this.banned = banned;
	}

	public Instant getUnban()
	{
		return unban;
	}

	public void setUnban(Instant unbanned)
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

	private Instant joined;
	private Instant login;
	private Instant logout;
	private Instant banned;
	private UUID banningPlayerUUID;
	private Instant unban;
	private String banReason;
}
