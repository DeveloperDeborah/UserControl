package no.runsafe.UserControl.command;

import no.runsafe.framework.api.command.ICommandExecutor;
import no.runsafe.framework.internal.command.ExecutableCommand;
import no.runsafe.framework.minecraft.RunsafeServer;
import no.runsafe.framework.minecraft.player.RunsafeAmbiguousPlayer;
import no.runsafe.framework.minecraft.player.RunsafePlayer;

import java.util.HashMap;

public class SuDo extends ExecutableCommand
{
	public SuDo()
	{
		super("sudo", "Force a player to run a command", "runsafe.usercontrol.sudo", "player", "command");
		captureTail();
	}

	@Override
	public String OnExecute(ICommandExecutor executor, HashMap<String, String> parameters)
	{
		RunsafePlayer target = RunsafeServer.Instance.getPlayer(parameters.get("player"));
		if (target instanceof RunsafeAmbiguousPlayer)
			return target.toString();

		if (target.hasPermission("runsafe.usercontrol.sudo.immune"))
			return "You cannot make that user run commands";

		String command = parameters.get("command");
		target.getRawPlayer().performCommand(command);
		return String.format("Forced %s to run /%s", target.getName(), command);
	}
}
