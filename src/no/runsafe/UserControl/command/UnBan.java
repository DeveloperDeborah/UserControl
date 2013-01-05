package no.runsafe.UserControl.command;

import no.runsafe.UserControl.database.PlayerDatabase;
import no.runsafe.framework.command.RunsafeCommand;
import no.runsafe.framework.server.RunsafeServer;
import no.runsafe.framework.server.player.RunsafePlayer;

public class UnBan extends RunsafeCommand
{
	public UnBan(PlayerDatabase playerDatabase)
	{
		super("unban", "player", "reason");
		playerdb = playerDatabase;
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
			return String.format("Player %s is not banned.", player.getPrettyName());

		// TODO Log unbanning reason
		playerdb.logPlayerUnban(player);
		player.setBanned(false);
		return String.format("Player %s was unbanned.", player.getPrettyName());
	}

	PlayerDatabase playerdb;
}
