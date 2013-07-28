package no.runsafe.UserControl.command;

import no.runsafe.framework.api.command.ExecutableCommand;
import no.runsafe.framework.api.command.ICommandExecutor;
import no.runsafe.framework.api.command.argument.TrailingArgument;
import no.runsafe.framework.minecraft.RunsafeServer;
import no.runsafe.framework.minecraft.player.RunsafePlayer;

import java.util.Map;

public class KickAll extends ExecutableCommand
{
	public KickAll()
	{
		super("kickall", "Kicks all players from the server", "runsafe.usercontrol.kickall", new TrailingArgument("reason"));
	}

	@Override
	public String OnExecute(ICommandExecutor executor, Map<String, String> parameters)
	{
		String reason = parameters.get("reason");
		int n = 0;

		RunsafePlayer kicker = null;
		if (executor instanceof RunsafePlayer)
			kicker = (RunsafePlayer) executor;

		for (RunsafePlayer victim : RunsafeServer.Instance.getOnlinePlayers())
			if (kicker == null || (!kicker.shouldNotSee(victim) && !victim.getName().equals(executor.getName())))
			{
				if (!victim.hasPermission("runsafe.usercontrol.kick.immune"))
				{
					RunsafeServer.Instance.kickPlayer(kicker, victim, reason);
					n++;
				}
			}
		return String.format("Kicked %d players from the server.", n);
	}
}
