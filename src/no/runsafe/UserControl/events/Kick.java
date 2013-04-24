package no.runsafe.UserControl.events;

import no.runsafe.framework.event.player.IPlayerKickEvent;
import no.runsafe.framework.output.IOutput;
import no.runsafe.framework.server.event.player.RunsafePlayerKickEvent;

public class Kick implements IPlayerKickEvent
{
	public Kick(IOutput output)
	{
		this.output = output;
	}

	@Override
	public void OnPlayerKick(RunsafePlayerKickEvent event)
	{
		output.writeColoured("Leave message:" + event.getLeaveMessage());
	}

	private IOutput output;
}
