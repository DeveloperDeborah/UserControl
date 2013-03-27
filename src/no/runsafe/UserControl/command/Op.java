package no.runsafe.UserControl.command;

import no.runsafe.framework.command.ExecutableCommand;
import no.runsafe.framework.server.ICommandExecutor;
import no.runsafe.framework.server.RunsafeServer;
import no.runsafe.framework.server.player.RunsafeAmbiguousPlayer;
import no.runsafe.framework.server.player.RunsafePlayer;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;

public class Op extends ExecutableCommand
{
	public Op()
	{
		super("op", "Makes a player an server operator", "runsafe.op", "player");
	}

	@Override
	public String OnExecute(ICommandExecutor executor, HashMap<String, String> parameters)
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
