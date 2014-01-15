package no.runsafe.UserControl.command;

import no.runsafe.framework.api.IServer;
import no.runsafe.framework.api.command.ExecutableCommand;
import no.runsafe.framework.api.command.ICommandExecutor;
import no.runsafe.framework.api.command.argument.IArgumentList;
import no.runsafe.framework.api.command.argument.OnlinePlayerRequired;
import no.runsafe.framework.api.command.argument.RequiredArgument;
import no.runsafe.framework.api.player.IPlayer;

public class BanIP extends ExecutableCommand
{
	public BanIP(IServer server)
	{
		super(
			"banip", "Bans an IP from connecting to this server", "runsafe.usercontrol.ban.ip",
			new OnlinePlayerRequired(), new RequiredArgument("reason")
		);
		this.server = server;
	}

	@Override
	public String OnExecute(ICommandExecutor executor, IArgumentList parameters)
	{
		IPlayer victim = server.getPlayer(parameters.get("player"));
		if (victim == null)
			return "Player not found";

		if (victim.hasPermission("runsafe.usercontrol.ban.immune"))
			return "You cannot ban that player";

		String ip = victim.getIP();
		if (ip == null)
			return String.format("Unable to get IP for player %s", victim.getName());
		server.banIP(ip);
		return String.format("Banned IP %s from the server", ip);
	}

	private final IServer server;
}
