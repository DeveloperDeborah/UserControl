package no.runsafe.UserControl.command;

import no.runsafe.framework.api.IServer;
import no.runsafe.framework.api.command.ExecutableCommand;
import no.runsafe.framework.api.command.ICommandExecutor;
import no.runsafe.framework.api.command.argument.TrailingArgument;
import no.runsafe.framework.api.player.IPlayer;

import java.util.Map;

public class KickAll extends ExecutableCommand
{
	public KickAll(IServer server)
	{
		super("kickall", "Kicks all players from the server", "runsafe.usercontrol.kickall", new TrailingArgument("reason"));
		this.server = server;
	}

	@Override
	public String OnExecute(ICommandExecutor executor, Map<String, String> parameters)
	{
		String reason = parameters.get("reason");
		int n = 0;

		IPlayer kicker = null;
		if (executor instanceof IPlayer)
			kicker = (IPlayer) executor;

		for (IPlayer victim : server.getOnlinePlayers())
			if (kicker == null || (!kicker.shouldNotSee(victim) && !victim.getName().equals(executor.getName())))
			{
				if (!victim.hasPermission("runsafe.usercontrol.kick.immune"))
				{
					server.kickPlayer(kicker, victim, reason);
					n++;
				}
			}
		return String.format("Kicked %d players from the server.", n);
	}

	private final IServer server;
}
