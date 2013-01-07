package no.runsafe.UserControl.command;

import no.runsafe.framework.command.RunsafeCommand;
import no.runsafe.framework.server.RunsafeServer;
import no.runsafe.framework.server.player.RunsafeAmbiguousPlayer;
import no.runsafe.framework.server.player.RunsafePlayer;
import org.apache.commons.lang.StringUtils;

public class DeOp extends RunsafeCommand
{
	public DeOp()
	{
		super("deop", "player");
	}

	@Override
	public String requiredPermission()
	{
		return "runsafe.op";
	}

	@Override
	public String OnExecute(RunsafePlayer executor, String[] args)
	{
		RunsafePlayer player = RunsafeServer.Instance.getPlayer(getArg("player"));
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
