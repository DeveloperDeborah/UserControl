package no.runsafe.UserControl.command;

import no.runsafe.framework.api.IServer;
import no.runsafe.framework.api.command.ExecutableCommand;
import no.runsafe.framework.api.command.ICommandExecutor;
import no.runsafe.framework.api.command.argument.PlayerArgument;
import no.runsafe.framework.api.player.IAmbiguousPlayer;
import no.runsafe.framework.api.player.IPlayer;

import java.util.Map;

public class DeOp extends ExecutableCommand
{
	public DeOp(IServer server)
	{
		super("deop", "Revokes server operator status from a player", "runsafe.op", new PlayerArgument());
		this.server = server;
	}

	@Override
	public String OnExecute(ICommandExecutor executor, Map<String, String> parameters)
	{
		IPlayer player = server.getPlayer(parameters.get("player"));
		if (player instanceof IAmbiguousPlayer)
			return player.toString();
		if (!player.isOP())
			return String.format("%s was not an operator.", player.getPrettyName());
		player.deOP();
		return String.format("%s is no longer an operator.", player.getPrettyName());
	}

	private final IServer server;
}
