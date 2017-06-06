package no.runsafe.UserControl.events;

import no.runsafe.UserControl.LoginRedirectManager;
import no.runsafe.framework.api.IConfiguration;
import no.runsafe.framework.api.ILocation;
import no.runsafe.framework.api.event.player.IPlayerJoinEvent;
import no.runsafe.framework.api.event.player.IPlayerPreLoginEvent;
import no.runsafe.framework.api.event.plugin.IConfigurationChanged;
import no.runsafe.framework.api.player.IPlayer;
import no.runsafe.framework.minecraft.event.player.RunsafePlayerJoinEvent;
import no.runsafe.framework.minecraft.event.player.RunsafePlayerPreLoginEvent;

import java.util.ArrayList;
import java.util.List;

public class Login implements IPlayerJoinEvent, IConfigurationChanged, IPlayerPreLoginEvent
{
	public Login(LoginRedirectManager loginRedirectManager)
	{
		this.loginRedirectManager = loginRedirectManager;
	}

	@Override
	public void OnBeforePlayerLogin(RunsafePlayerPreLoginEvent event)
	{
		if (event.getPlayer().isNew())
			this.newPlayers.add(event.getPlayer());
	}

	@Override
	public void OnPlayerJoinEvent(RunsafePlayerJoinEvent event)
	{
		IPlayer player = event.getPlayer();
		if (this.loginRedirectManager.hasRedirectLocation())
		{
			player.teleport(this.loginRedirectManager.getRedirectLocation());
		}
		else if (this.newPlayers.contains(player) && firstSpawnLocation != null)
		{
			player.teleport(firstSpawnLocation);
			this.newPlayers.remove(player);
		}
	}

	@Override
	public void OnConfigurationChanged(IConfiguration config)
	{
		this.firstSpawnLocation = config.getConfigValueAsLocation("firstJoinLocation");
	}

	private final List<IPlayer> newPlayers = new ArrayList<>();
	private final LoginRedirectManager loginRedirectManager;
	private ILocation firstSpawnLocation;
}