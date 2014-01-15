package no.runsafe.UserControl.command;

import no.runsafe.framework.api.IServer;
import no.runsafe.framework.api.command.ExecutableCommand;
import no.runsafe.framework.api.command.ICommandExecutor;
import no.runsafe.framework.api.command.argument.IArgumentList;
import no.runsafe.framework.api.command.argument.OnlinePlayerRequired;
import no.runsafe.framework.api.command.argument.TrailingArgument;
import no.runsafe.framework.api.player.IPlayer;

public class SuDo extends ExecutableCommand
{
	public SuDo(IServer server)
	{
		super("sudo", "Force a player to run a command", "runsafe.usercontrol.sudo", new OnlinePlayerRequired(), new TrailingArgument("command"));
		this.server = server;
	}

	@Override
	public String OnExecute(ICommandExecutor executor, IArgumentList parameters)
	{
		IPlayer target = server.getPlayer(parameters.get("player"));

		if (target.hasPermission("runsafe.usercontrol.sudo.immune"))
			return "You cannot make that user run commands";

		String command = parameters.get("command");
		target.performCommand(command);
		return String.format("Forced %s to run /%s", target.getName(), command);
	}

	private final IServer server;
}
