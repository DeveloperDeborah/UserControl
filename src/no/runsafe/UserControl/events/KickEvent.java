package no.runsafe.UserControl.events;

import no.runsafe.framework.configuration.IConfiguration;
import no.runsafe.framework.event.IConfigurationChanged;
import no.runsafe.framework.event.player.IPlayerKickEvent;
import no.runsafe.framework.output.IOutput;
import no.runsafe.framework.server.event.player.RunsafePlayerKickEvent;

public class KickEvent implements IPlayerKickEvent
{
	@Override
	public void OnPlayerKick(RunsafePlayerKickEvent event)
	{
		if (event.getKicker() != null)
			event.setLeaveMessage(null);
	}
}
