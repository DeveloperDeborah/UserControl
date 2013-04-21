package no.runsafe.UserControl.events;

import no.runsafe.UserControl.LoginRedirectManager;
import no.runsafe.framework.event.player.IPlayerJoinEvent;
import no.runsafe.framework.server.event.player.RunsafePlayerJoinEvent;
import no.runsafe.framework.server.player.RunsafePlayer;

public class Login implements IPlayerJoinEvent
{
	public Login(LoginRedirectManager loginRedirectManager)
	{
		this.loginRedirectManager = loginRedirectManager;
	}

	@Override
	public void OnPlayerJoinEvent(RunsafePlayerJoinEvent event)
	{
		RunsafePlayer player = event.getPlayer();
		if (this.loginRedirectManager.hasRedirectLocation())
			player.teleport(this.loginRedirectManager.getRedirectLocation());
	}

	private LoginRedirectManager loginRedirectManager;
}
