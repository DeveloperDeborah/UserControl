package no.runsafe.UserControl.command;

import no.runsafe.UserControl.database.PlayerKickLog;
import no.runsafe.framework.command.RunsafeCommand;
import no.runsafe.framework.configuration.IConfiguration;
import no.runsafe.framework.event.IConfigurationChanged;
import no.runsafe.framework.server.RunsafeServer;
import no.runsafe.framework.server.player.RunsafePlayer;
import org.apache.commons.lang.StringUtils;

public class Kick extends RunsafeCommand
{
	public Kick(PlayerKickLog log)
	{
		super("kick", "player", "reason");
		logger = log;
	}

	@Override
	public String requiredPermission()
	{
		return "runsafe.usercontrol.kick";
	}

	@Override
	public String OnExecute(RunsafePlayer executor, String[] args)
	{
		RunsafePlayer victim = RunsafeServer.Instance.getPlayer(getArg("player"));
		if(victim == null)
			return "Player not found";

		if(victim.hasPermission("runsafe.usercontrol.kick.immune"))
			return "You cannot kick that player";

		String reason = StringUtils.join(args, " ", 1, args.length);
		RunsafeServer.Instance.kickPlayer(executor, victim, reason);
		return null;
	}

	PlayerKickLog logger;
}
