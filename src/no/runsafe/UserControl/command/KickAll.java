package no.runsafe.UserControl.command;

import no.runsafe.framework.api.command.ExecutableCommand;
import no.runsafe.framework.api.command.ICommandExecutor;
import no.runsafe.framework.api.command.argument.IArgumentList;
import no.runsafe.framework.api.command.argument.TrailingArgument;
import no.runsafe.framework.api.player.IPlayer;
import no.runsafe.framework.api.server.IPlayerManager;
import no.runsafe.framework.api.server.IPlayerProvider;

public class KickAll extends ExecutableCommand
{
	public KickAll(IPlayerManager playerManager, IPlayerProvider playerProvider)
	{
		super("kickall", "Kicks all players from the server", "runsafe.usercontrol.kickall", new TrailingArgument("reason"));
		this.playerManager = playerManager;
		this.playerProvider = playerProvider;
	}

	@Override
	public String OnExecute(ICommandExecutor executor, IArgumentList parameters)
	{
		String reason = parameters.getValue("reason");
		int n = 0;

		IPlayer kicker = null;
		if (executor instanceof IPlayer)
			kicker = (IPlayer) executor;

		for (IPlayer victim : playerProvider.getOnlinePlayers())
			if (kicker == null || (!kicker.shouldNotSee(victim) && !victim.getName().equals(executor.getName())))
			{
				if (!victim.hasPermission("runsafe.usercontrol.kick.immune"))
				{
					playerManager.kickPlayer(kicker, victim, reason);
					n++;
				}
			}
		return String.format("Kicked %d players from the server.", n);
	}

	private final IPlayerManager playerManager;
	private final IPlayerProvider playerProvider;
}
