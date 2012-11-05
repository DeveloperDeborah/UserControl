package no.runsafe.UserControl.command;

import no.runsafe.framework.command.RunsafeCommand;
import no.runsafe.framework.configuration.IConfiguration;
import no.runsafe.framework.event.IConfigurationChanged;

public class TempBan extends RunsafeCommand implements IConfigurationChanged
{
	public TempBan()
	{
		super("tempban", "player", "time", "reason");
	}

	@Override
	public void OnConfigurationChanged(IConfiguration configuration)
	{
		banMessage = configuration.getConfigValueAsString("messages.tempban");
	}

	String banMessage = "%3$s";
}
