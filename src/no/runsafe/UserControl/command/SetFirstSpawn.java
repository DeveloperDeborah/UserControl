package no.runsafe.UserControl.command;

import no.runsafe.framework.command.player.PlayerCommand;
import no.runsafe.framework.configuration.IConfiguration;
import no.runsafe.framework.server.RunsafeLocation;
import no.runsafe.framework.server.player.RunsafePlayer;

import java.util.HashMap;

public class SetFirstSpawn extends PlayerCommand
{
	public SetFirstSpawn(IConfiguration config)
	{
		super("setfirstspawn", "Sets the first-join spawn", "runsafe.usercontrol.setfirstspawn");
		this.config = config;
	}

	@Override
	public String OnExecute(RunsafePlayer executor, HashMap<String, String> parameters)
	{
		RunsafeLocation location = executor.getLocation();
		this.config.setConfigValue("firstJoinLocation.world", location.getWorld().getName());
		this.config.setConfigValue("firstJoinLocation.x", location.getX());
		this.config.setConfigValue("firstJoinLocation.y", location.getY());
		this.config.setConfigValue("firstJoinLocation.z", location.getZ());
		this.config.setConfigValue("firstJoinLocation.yaw", location.getYaw());
		this.config.setConfigValue("firstJoinLocation.pitch", location.getPitch());
		return "&2The location for first-join server has been set.";
	}

	private IConfiguration config;
}
