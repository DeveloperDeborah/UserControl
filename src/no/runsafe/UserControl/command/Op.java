package no.runsafe.UserControl.command;

import no.runsafe.framework.api.command.ExecutableCommand;
import no.runsafe.framework.api.command.ICommandExecutor;
import no.runsafe.framework.api.command.argument.IArgumentList;
import no.runsafe.framework.api.command.argument.Player;
import no.runsafe.framework.api.player.IPlayer;

public class Op extends ExecutableCommand
{
	public Op()
	{
		super("op", "Makes a player an server operator", "runsafe.op", new Player().onlineOnly().defaultToExecutor());
	}

	@Override
	public String OnExecute(ICommandExecutor executor, IArgumentList parameters)
	{
		IPlayer player = parameters.getValue("player");
		if (player == null)
			return null;

		if (player.isOP())
			return String.format("%s is already an operator.", player.getPrettyName());
		player.OP();
		return String.format("%s is now an operator.", player.getPrettyName());
	}
}
