package no.runsafe.UserControl.command;

import no.runsafe.framework.api.IServer;
import no.runsafe.framework.api.command.ExecutableCommand;
import no.runsafe.framework.api.command.ICommandExecutor;
import no.runsafe.framework.api.command.argument.IArgumentList;
import no.runsafe.framework.api.command.argument.RequiredArgument;

public class UnBanIP extends ExecutableCommand
{
	public UnBanIP(IServer server)
	{
		super(
			"unbanip", "Bans an IP from connecting to this server", "runsafe.usercontrol.unban.ip",
			new RequiredArgument("ip-address")
		);
		this.server = server;
	}

	@Override
	public String OnExecute(ICommandExecutor executor, IArgumentList parameters)
	{
		String ip = parameters.getValue("ip-address");
		server.unbanIP(ip);
		return String.format("Unbanned IP %s from the server", ip);
	}

	private final IServer server;
}
