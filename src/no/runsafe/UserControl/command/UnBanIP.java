package no.runsafe.UserControl.command;

import no.runsafe.framework.api.command.ExecutableCommand;
import no.runsafe.framework.api.command.ICommandExecutor;
import no.runsafe.framework.api.command.argument.IArgumentList;
import no.runsafe.framework.api.command.argument.RequiredArgument;
import no.runsafe.framework.api.server.IPlayerManager;

public class UnBanIP extends ExecutableCommand
{
	public UnBanIP(IPlayerManager playerManager)
	{
		super(
			"unbanip", "Bans an IP from connecting to this server", "runsafe.usercontrol.unban.ip",
			new RequiredArgument("ip-address")
		);
		this.playerManager = playerManager;
	}

	@Override
	public String OnExecute(ICommandExecutor executor, IArgumentList parameters)
	{
		String ip = parameters.getValue("ip-address");
		playerManager.unbanIP(ip);
		return String.format("Unbanned IP %s from the server", ip);
	}

	private final IPlayerManager playerManager;
}
