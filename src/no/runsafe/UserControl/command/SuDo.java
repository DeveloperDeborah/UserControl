package no.runsafe.UserControl.command;

import no.runsafe.framework.api.IServer;
import no.runsafe.framework.api.command.ExecutableCommand;
import no.runsafe.framework.api.command.ICommandExecutor;
import no.runsafe.framework.api.command.argument.PlayerArgument;
import no.runsafe.framework.api.command.argument.TrailingArgument;
import no.runsafe.framework.api.player.IAmbiguousPlayer;
import no.runsafe.framework.api.player.IPlayer;
import no.runsafe.framework.internal.wrapper.ObjectUnwrapper;
import org.bukkit.entity.Player;

import java.util.Map;

public class SuDo extends ExecutableCommand
{
	public SuDo(IServer server)
	{
		super("sudo", "Force a player to run a command", "runsafe.usercontrol.sudo", new PlayerArgument(), new TrailingArgument("command"));
		this.server = server;
	}

	@Override
	public String OnExecute(ICommandExecutor executor, Map<String, String> parameters)
	{
		IPlayer target = server.getPlayer(parameters.get("player"));
		if (target instanceof IAmbiguousPlayer)
			return target.toString();

		if (target.hasPermission("runsafe.usercontrol.sudo.immune"))
			return "You cannot make that user run commands";

		String command = parameters.get("command");
		((Player) ObjectUnwrapper.convert(target)).performCommand(command);
		return String.format("Forced %s to run /%s", target.getName(), command);
	}

	private final IServer server;
}
