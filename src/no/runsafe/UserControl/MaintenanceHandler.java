package no.runsafe.UserControl;

import no.runsafe.framework.api.IConfiguration;
import no.runsafe.framework.api.event.player.IPlayerPreLoginEvent;
import no.runsafe.framework.api.event.plugin.IConfigurationChanged;
import no.runsafe.framework.api.player.IPlayer;
import no.runsafe.framework.minecraft.event.player.RunsafePlayerPreLoginEvent;

public class MaintenanceHandler implements IConfigurationChanged, IPlayerPreLoginEvent
{
	public boolean blockConnection(IPlayer player)
	{
		return !player.hasPermission("runsafe.usercontrol.maintenance.override");
	}

	public boolean isMaintenance()
	{
		return this.isMaintenance;
	}

	public void setMaintenance(boolean state)
	{
		this.isMaintenance = state;
		if (this.config != null)
		{
			this.config.setConfigValue("maintenance", state);
			this.config.save();
		}
	}

	@Override
	public void OnConfigurationChanged(IConfiguration config)
	{
		this.config = config;
		this.isMaintenance = config.getConfigValueAsBoolean("maintenance");
	}

	@Override
	public void OnBeforePlayerLogin(RunsafePlayerPreLoginEvent event)
	{
		if (this.isMaintenance() && this.blockConnection(event.getPlayer()))
			event.disallow("The server is currently down for important maintenance, please check back shortly!");
	}

	private boolean isMaintenance;
	private IConfiguration config;
}
