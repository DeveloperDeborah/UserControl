package no.runsafe.UserControl.events;

import no.runsafe.UserControl.LoginRedirectManager;
import no.runsafe.framework.api.IConfiguration;
import no.runsafe.framework.api.ILocation;
import no.runsafe.framework.api.IWorld;
import no.runsafe.framework.api.event.player.IPlayerJoinEvent;
import no.runsafe.framework.api.event.player.IPlayerPreLoginEvent;
import no.runsafe.framework.api.event.plugin.IConfigurationChanged;
import no.runsafe.framework.api.player.IPlayer;
import no.runsafe.framework.minecraft.RunsafeLocation;
import no.runsafe.framework.minecraft.RunsafeServer;
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
			this.newPlayers.add(event.getPlayer().getName());
	}

	@Override
	public void OnPlayerJoinEvent(RunsafePlayerJoinEvent event)
	{
		IPlayer player = event.getPlayer();
		if (this.loginRedirectManager.hasRedirectLocation())
		{
			player.teleport(this.loginRedirectManager.getRedirectLocation());
		}
		else if (this.newPlayers.contains(player.getName()) && firstSpawnLocation != null)
		{
			player.teleport(firstSpawnLocation);
			this.newPlayers.remove(player.getName());
		}
	}

	@Override
	public void OnConfigurationChanged(IConfiguration config)
	{
		String initialWorld = config.getConfigValueAsString("firstJoinLocation.world");
		this.firstSpawnLocation = null;
		if (initialWorld != null)
		{
			IWorld world = RunsafeServer.Instance.getWorld(initialWorld);
			if (world != null)
				this.firstSpawnLocation = new RunsafeLocation(
					world,
					config.getConfigValueAsDouble("firstJoinLocation.x"),
					config.getConfigValueAsDouble("firstJoinLocation.y"),
					config.getConfigValueAsDouble("firstJoinLocation.z"),
					config.getConfigValueAsFloat("firstJoinLocation.yaw"),
					config.getConfigValueAsFloat("firstJoinLocation.pitch")
				);
		}
	}

	private final List<String> newPlayers = new ArrayList<String>();
	private ILocation firstSpawnLocation;
	private final LoginRedirectManager loginRedirectManager;
}
