package no.runsafe.UserControl.command;

import no.runsafe.framework.api.command.ExecutableCommand;
import no.runsafe.framework.api.command.ICommandExecutor;
import no.runsafe.framework.minecraft.RunsafeServer;
import no.runsafe.framework.minecraft.player.RunsafeAmbiguousPlayer;
import no.runsafe.framework.minecraft.player.RunsafePlayer;

import java.util.Map;

public class Op extends ExecutableCommand
{
	public Op()
	{
		super("op", "Makes a player an server operator", "runsafe.op", "player");
	}

	@Override
	public String OnExecute(ICommandExecutor executor, Map<String, String> parameters)
	{
		RunsafePlayer player = RunsafeServer.Instance.getPlayer(parameters.get("player"));
		if (player instanceof RunsafeAmbiguousPlayer)
			return player.toString();

		if (player.isOP())
			return String.format("%s is already an operator.", player.getPrettyName());
		player.OP();
		return String.format("%s is now an operator.", player.getPrettyName());
	}
}
