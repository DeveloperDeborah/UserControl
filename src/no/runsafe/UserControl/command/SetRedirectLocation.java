package no.runsafe.UserControl.command;

import no.runsafe.UserControl.LoginRedirectManager;
import no.runsafe.framework.command.ExecutableCommand;
import no.runsafe.framework.command.player.PlayerCommand;
import no.runsafe.framework.server.ICommandExecutor;
import no.runsafe.framework.server.player.RunsafePlayer;

import java.util.HashMap;

public class SetRedirectLocation extends PlayerCommand
{
	public SetRedirectLocation(LoginRedirectManager loginRedirectManager)
	{
		super("setredirectlocation", "Sets a server-wide redirection location for when players log-in", "runsafe.usercontrol.setredirectlocation");
		this.loginRedirectManager = loginRedirectManager;
	}

	@Override
	public String OnExecute(RunsafePlayer executor, HashMap<String, String> parameters)
	{
		this.loginRedirectManager.setRedirectLocation(executor.getLocation());
		return null;
	}

	private LoginRedirectManager loginRedirectManager;
}
