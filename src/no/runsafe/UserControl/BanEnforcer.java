package no.runsafe.UserControl;

import no.runsafe.UserControl.database.IPBanList;
import no.runsafe.UserControl.database.PlayerData;
import no.runsafe.UserControl.database.PlayerDatabase;
import no.runsafe.framework.configuration.IConfiguration;
import no.runsafe.framework.event.IConfigurationChanged;
import no.runsafe.framework.event.player.IPlayerLoginEvent;
import no.runsafe.framework.server.event.player.RunsafePlayerLoginEvent;

public class BanEnforcer implements IPlayerLoginEvent, IConfigurationChanged
{
	public BanEnforcer(PlayerDatabase playerDatabase, IPBanList ipBanList)
	{
		playerdb = playerDatabase;
		ipBan = ipBanList;
	}

	@Override
	public void OnPlayerLogin(RunsafePlayerLoginEvent event)
	{
		PlayerData data = playerdb.getData(event.getPlayer());
		if(data.getBanned() != null)
			event.playerBanned(String.format(banMessageFormat, data.getBanReason()));
		//String banReason = ipBan.getIpBan(event.getPlayer());
		//if(banReason != null)
		//	event.playerBanned(String.format(banMessageFormat, banReason));
	}

	@Override
	public void OnConfigurationChanged(IConfiguration configuration)
	{
		banMessageFormat = configuration.getConfigValueAsString("messages.ban");
	}

	PlayerDatabase playerdb;
	IPBanList ipBan;
	String banMessageFormat = "Banned: %s";
}
