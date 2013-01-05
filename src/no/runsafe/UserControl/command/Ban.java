package no.runsafe.UserControl.command;

import no.runsafe.UserControl.database.PlayerDatabase;
import no.runsafe.UserControl.database.PlayerKickLog;
import no.runsafe.framework.command.ICommand;
import no.runsafe.framework.command.RunsafeCommand;
import no.runsafe.framework.configuration.IConfiguration;
import no.runsafe.framework.event.IConfigurationChanged;
import no.runsafe.framework.server.RunsafeServer;
import no.runsafe.framework.server.player.RunsafeAmbiguousPlayer;
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
		String reason = StringUtils.join(args, " ", 1, args.length);
		RunsafePlayer victim = RunsafeServer.Instance.getPlayer(getArg("player"));
		if(victim == null)
			return "Player not found";

		if (victim instanceof RunsafeAmbiguousPlayer)
		{
			return String.format(
				"Multiple matches found: %s",
				StringUtils.join(((RunsafeAmbiguousPlayer) victim).getAmbiguity(), ", ")
			);
		}

		if(victim.hasPermission("runsafe.usercontrol.ban.immune"))
			return "You cannot ban that player";

		if (!victim.isOnline() || (executor != null && !executor.canSee(victim)))
		{
			victim.setBanned(true);
			playerdb.logPlayerBan(victim, executor, reason);
			logger.logKick(executor, victim, reason, true);
			return String.format("Banned offline player %s.", victim.getPrettyName());
		}
		if(lightning)
			victim.strikeWithLightning(fakeLightning);
		RunsafeServer.Instance.banPlayer(executor, victim, reason);
		return null;
	}

	@Override
	public void OnConfigurationChanged(IConfiguration configuration)
	{
		lightning = configuration.getConfigValueAsBoolean("ban.lightning.strike");
		fakeLightning = !configuration.getConfigValueAsBoolean("ban.lightning.real");
	}

	PlayerKickLog logger;
	PlayerDatabase playerdb;
	boolean lightning;
	boolean fakeLightning;
}
