package no.runsafe.UserControl.events;

import no.runsafe.framework.api.event.player.IPlayerKickEvent;
import no.runsafe.framework.minecraft.event.player.RunsafePlayerKickEvent;

public class KickEvent implements IPlayerKickEvent
{
	@Override
	public void OnPlayerKick(RunsafePlayerKickEvent event)
	{
		event.setLeaveMessage("");
	}
}
