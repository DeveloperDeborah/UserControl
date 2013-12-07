package no.runsafe.UserControl.command;

import no.runsafe.framework.api.command.ExecutableCommand;
import no.runsafe.framework.api.command.ICommandExecutor;
import no.runsafe.framework.api.command.argument.PlayerArgument;
import no.runsafe.framework.api.player.IPlayer;
import no.runsafe.framework.minecraft.RunsafeServer;
import no.runsafe.framework.minecraft.player.RunsafeAmbiguousPlayer;

import java.util.Map;

public class DeOp extends ExecutableCommand
{
	public DeOp()
	{
		super("deop", "Revokes server operator status from a player", "runsafe.op", new PlayerArgument());
	}

	@Override
	public String OnExecute(ICommandExecutor executor, Map<String, String> parameters)
	{
		IPlayer player = RunsafeServer.Instance.getPlayer(parameters.get("player"));
		if (player instanceof RunsafeAmbiguousPlayer)
			return player.toString();
		if (!player.isOP())
			return String.format("%s was not an operator.", player.getPrettyName());
		player.deOP();
		return String.format("%s is no longer an operator.", player.getPrettyName());
	}
}
