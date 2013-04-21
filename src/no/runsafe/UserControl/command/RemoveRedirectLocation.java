package no.runsafe.UserControl.command;

import no.runsafe.UserControl.LoginRedirectManager;
import no.runsafe.framework.command.player.PlayerCommand;
import no.runsafe.framework.server.player.RunsafePlayer;

import java.util.HashMap;

public class RemoveRedirectLocation extends PlayerCommand
{
	public RemoveRedirectLocation(LoginRedirectManager loginRedirectManager)
	{
		super("removeredirectlocation", "Removes the re-direction location", "runsafe.usercontrol.removeredirectlocation");
		this.loginRedirectManager = loginRedirectManager;
	}

	@Override
	public String OnExecute(RunsafePlayer executor, HashMap<String, String> parameters)
	{
		this.loginRedirectManager.removeRedirectLocation();
		executor.sendMessage("Re-direction location has been removed.");
		return null;
	}

	private LoginRedirectManager loginRedirectManager;
}
