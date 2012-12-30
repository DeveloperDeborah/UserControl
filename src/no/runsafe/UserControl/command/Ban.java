package no.runsafe.UserControl.command;

import no.runsafe.UserControl.database.PlayerDatabase;
import no.runsafe.UserControl.database.PlayerKickLog;
import no.runsafe.framework.command.ICommand;
import no.runsafe.framework.command.RunsafeCommand;
import no.runsafe.framework.configuration.IConfiguration;
import no.runsafe.framework.event.IConfigurationChanged;
import no.runsafe.framework.server.RunsafeServer;
import no.runsafe.framework.server.player.RunsafePlayer;
import org.apache.commons.lang.StringUtils;

import java.util.Collection;

public class Ban extends RunsafeCommand implements IConfigurationChanged
{
	public Ban(PlayerKickLog log, PlayerDatabase playerDatabase)
	{
		super("ban", "player", "reason");
		logger = log;
		playerdb = playerDatabase;
	}

	@Override
	public String requiredPermission()
	{
		return "runsafe.usercontrol.ban";
	}

	@Override
	public String OnExecute(RunsafePlayer executor, String[] args)
	{
		String source = executor == null ? "console" : executor.getName();
		String reason = StringUtils.join(args, " ", 1, args.length);
		RunsafePlayer victim = RunsafeServer.Instance.getPlayer(getArg("player"));
		if(victim == null)
			return "Player not found";

		if(victim.hasPermission("runsafe.usercontrol.ban.immune"))
			return "You cannot ban that player";

		playerdb.logPlayerBan(victim, executor, reason);
		if (!victim.isOnline() || (executor != null && !executor.canSee(victim)))
		{
			victim.setBanned(true);
			logger.logKick(executor, victim, reason, true);
			return String.format("Banned offline player %s.", getArg("player"));
		}
		victim.kick(String.format(banMessageFormat, source, reason));
		victim.setBanned(true);
		logger.logKick(executor, victim, reason, true);
		return null;
	}

	@Override
	public void OnConfigurationChanged(IConfiguration configuration)
	{
		banMessageFormat = configuration.getConfigValueAsString("messages.ban");
	}

	String banMessageFormat = "%2$s";
	PlayerKickLog logger;
	PlayerDatabase playerdb;
}
