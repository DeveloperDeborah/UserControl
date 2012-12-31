package no.runsafe.UserControl;

import no.runsafe.UserControl.database.PlayerData;
import no.runsafe.UserControl.database.PlayerDatabase;
import no.runsafe.framework.configuration.IConfiguration;
import no.runsafe.framework.event.IConfigurationChanged;
import no.runsafe.framework.event.player.IPlayerLoginEvent;
import no.runsafe.framework.server.event.player.RunsafePlayerLoginEvent;

public class BanEnforcer implements IPlayerLoginEvent, IConfigurationChanged
{
	public BanEnforcer(PlayerDatabase playerDatabase)
	{
		playerdb = playerDatabase;
	}

	@Override
	public void OnPlayerLogin(RunsafePlayerLoginEvent event)
	{
		PlayerData data = playerdb.getData(event.getPlayer());
		if (data.getBanned() != null)
			event.playerBanned(String.format(banMessageFormat, data.getBanReason()));
	}

	@Override
	public void OnConfigurationChanged(IConfiguration configuration)
	{
		banMessageFormat = configuration.getConfigValueAsString("messages.ban");
	}

	PlayerDatabase playerdb;
	String banMessageFormat = "Banned: %s";
}
