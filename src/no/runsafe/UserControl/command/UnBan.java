package no.runsafe.UserControl.command;

import no.runsafe.framework.command.RunsafeCommand;
import no.runsafe.framework.server.RunsafeServer;
import no.runsafe.framework.server.player.RunsafePlayer;

public class UnBan extends RunsafeCommand
{
	public UnBan()
	{
		super("unban", "player", "reason");
	}

	@Override
	public String requiredPermission()
	{
		return "runsafe.usercontrol.unban";
	}

	@Override
	public String OnExecute(RunsafePlayer executor, String[] args)
	{
		RunsafePlayer player = RunsafeServer.Instance.getPlayer(getArg("player"));
		if(!player.isBanned())
			return String.format("Player %s is not banned.", player.getName());

		// TODO Log unbanning
		player.setBanned(false);
		return String.format("Player %s was unbanned.", player.getName());
	}
}
