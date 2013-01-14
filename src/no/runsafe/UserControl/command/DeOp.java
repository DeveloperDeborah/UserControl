package no.runsafe.UserControl.command;

import no.runsafe.framework.command.ExecutableCommand;
import no.runsafe.framework.server.ICommandExecutor;
import no.runsafe.framework.server.RunsafeServer;
import no.runsafe.framework.server.player.RunsafeAmbiguousPlayer;
import no.runsafe.framework.server.player.RunsafePlayer;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;

public class DeOp extends ExecutableCommand
{
	public DeOp()
	{
		super("deop", "Revokes server operator status from a player", "runsafe.op", "player");
	}

	@Override
	public String OnExecute(ICommandExecutor executor, HashMap<String, String> parameters, String[] arguments)
	{
		RunsafePlayer player = RunsafeServer.Instance.getPlayer(parameters.get("player"));
		if (player instanceof RunsafeAmbiguousPlayer)
		{
			return String.format(
				"Multiple matches found: %s",
				StringUtils.join(((RunsafeAmbiguousPlayer) player).getAmbiguity(), ", ")
			);
		}
		if (!player.isOP())
			return String.format("%s was not an operator.", player.getPrettyName());
		player.deOP();
		return String.format("%s is no longer an operator.", player.getPrettyName());
	}
}
