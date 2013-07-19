package no.runsafe.UserControl.command;

import no.runsafe.framework.api.IConfiguration;
import no.runsafe.framework.api.command.player.PlayerCommand;
import no.runsafe.framework.minecraft.RunsafeLocation;
import no.runsafe.framework.minecraft.player.RunsafePlayer;

import java.util.Map;

public class SetFirstSpawn extends PlayerCommand
{
	public SetFirstSpawn(IConfiguration config)
	{
		super("setfirstspawn", "Sets the first-join spawn", "runsafe.usercontrol.setfirstspawn");
		this.config = config;
	}

	@Override
	public String OnExecute(RunsafePlayer executor, Map<String, String> parameters)
	{
		RunsafeLocation location = executor.getLocation();
		this.config.setConfigValue("firstJoinLocation.world", location.getWorld().getName());
		this.config.setConfigValue("firstJoinLocation.x", location.getX());
		this.config.setConfigValue("firstJoinLocation.y", location.getY());
		this.config.setConfigValue("firstJoinLocation.z", location.getZ());
		this.config.setConfigValue("firstJoinLocation.yaw", location.getYaw());
		this.config.setConfigValue("firstJoinLocation.pitch", location.getPitch());
		this.config.save();
		return "&2The location for first-join server has been set.";
	}

	private final IConfiguration config;
}
