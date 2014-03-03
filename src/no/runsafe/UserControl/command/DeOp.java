package no.runsafe.UserControl.command;

import no.runsafe.framework.api.command.ExecutableCommand;
import no.runsafe.framework.api.command.ICommandExecutor;
import no.runsafe.framework.api.command.argument.IArgumentList;
import no.runsafe.framework.api.command.argument.Player;
import no.runsafe.framework.api.player.IPlayer;

public class DeOp extends ExecutableCommand
{
	public DeOp()
	{
		super("deop", "Revokes server operator status from a player", "runsafe.op", new Player.Online("player", true));
	}

	@Override
	public String OnExecute(ICommandExecutor executor, IArgumentList parameters)
	{
		IPlayer player = parameters.getValue("player");
		if (player == null)
			return null;
		if (!player.isOP())
			return String.format("%s was not an operator.", player.getPrettyName());
		player.deOP();
		return String.format("%s is no longer an operator.", player.getPrettyName());
	}
}
