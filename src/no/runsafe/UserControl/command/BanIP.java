package no.runsafe.UserControl.command;

import no.runsafe.framework.command.RunsafeCommand;
import no.runsafe.framework.configuration.IConfiguration;
import no.runsafe.framework.event.IConfigurationChanged;

public class BanIP extends RunsafeCommand implements IConfigurationChanged
{
	public BanIP()
	{
		super("banip", "player", "reason");
	}

	@Override
	public void OnConfigurationChanged(IConfiguration configuration)
	{
		banMessage = configuration.getConfigValueAsString("messages.ban");
	}

	String banMessage = "%2$s";
}
