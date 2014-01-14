package no.runsafe.UserControl.command;

import no.runsafe.framework.api.IConfiguration;
import no.runsafe.framework.api.ILocation;
import no.runsafe.framework.api.command.argument.IArgumentList;
import no.runsafe.framework.api.command.player.PlayerCommand;
import no.runsafe.framework.api.event.plugin.IConfigurationChanged;
import no.runsafe.framework.api.player.IPlayer;

public class SetFirstSpawn extends PlayerCommand implements IConfigurationChanged
{
	public SetFirstSpawn()
	{
		super("setfirstspawn", "Sets the first-join spawn", "runsafe.usercontrol.setfirstspawn");
	}

	@Override
	public void OnConfigurationChanged(IConfiguration configuration)
	{
		config = configuration;
	}

	@Override
	public String OnExecute(IPlayer executor, IArgumentList parameters)
	{
		ILocation location = executor.getLocation();
		this.config.setConfigValue("firstJoinLocation.world", location.getWorld().getName());
		this.config.setConfigValue("firstJoinLocation.x", location.getX());
		this.config.setConfigValue("firstJoinLocation.y", location.getY());
		this.config.setConfigValue("firstJoinLocation.z", location.getZ());
		this.config.setConfigValue("firstJoinLocation.yaw", location.getYaw());
		this.config.setConfigValue("firstJoinLocation.pitch", location.getPitch());
		this.config.save();
		return "&2The location for first-join server has been set.";
	}

	private IConfiguration config;
}
