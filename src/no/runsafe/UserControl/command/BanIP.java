package no.runsafe.UserControl.command;

import no.runsafe.framework.api.command.ExecutableCommand;
import no.runsafe.framework.api.command.ICommandExecutor;
import no.runsafe.framework.api.command.argument.IArgumentList;
import no.runsafe.framework.api.command.argument.Player;
import no.runsafe.framework.api.command.argument.RequiredArgument;
import no.runsafe.framework.api.player.IPlayer;
import no.runsafe.framework.api.server.IPlayerManager;

public class BanIP extends ExecutableCommand
{
	public BanIP(IPlayerManager playerManager)
	{
		super(
			"banip", "Bans an IP from connecting to this server", "runsafe.usercontrol.ban.ip",
			new Player().onlineOnly().require(), new RequiredArgument("reason")
		);
		this.playerManager = playerManager;
	}

	@Override
	public String OnExecute(ICommandExecutor executor, IArgumentList parameters)
	{
		IPlayer victim = parameters.getValue("player");
		if (victim == null)
			return null;

		if (victim.hasPermission("runsafe.usercontrol.ban.immune"))
			return "You cannot ban that player";

		String ip = victim.getIP();
		if (ip == null)
			return String.format("Unable to get IP for player %s", victim.getName());
		playerManager.banIP(ip);
		return String.format("Banned IP %s from the server", ip);
	}

	private final IPlayerManager playerManager;
}
