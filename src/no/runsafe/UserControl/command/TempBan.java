package no.runsafe.UserControl.command;

import no.runsafe.UserControl.database.PlayerDatabase;
import no.runsafe.UserControl.database.PlayerKickLog;
import no.runsafe.framework.command.RunsafeCommand;
import no.runsafe.framework.configuration.IConfiguration;
import no.runsafe.framework.event.IConfigurationChanged;
import no.runsafe.framework.server.RunsafeServer;
import no.runsafe.framework.server.player.RunsafeAmbiguousPlayer;
import no.runsafe.framework.server.player.RunsafePlayer;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

public class TempBan extends RunsafeCommand implements IConfigurationChanged
{
	public TempBan(PlayerDatabase playerDatabase, PlayerKickLog logger)
	{
		super("tempban", "player", "time", "reason");
		this.logger = logger;
		timeParser = new PeriodFormatterBuilder()
			.printZeroRarelyFirst().appendYears().appendSuffix("y")
			.printZeroRarelyFirst().appendWeeks().appendSuffix("w", "weeks")
			.printZeroRarelyFirst().appendDays().appendSuffix("d")
			.printZeroRarelyFirst().appendHours().appendSuffix("h")
			.printZeroRarelyFirst().appendMinutes().appendSuffix("m")
			.printZeroRarelyFirst().appendSeconds().appendSuffix("s")
			.toFormatter();
		playerdb = playerDatabase;
	}

	@Override
	public String requiredPermission()
	{
		return "runsafe.usercontrol.ban.temporary";
	}

	@Override
	public String OnExecute(RunsafePlayer executor, String[] args)
	{
		try
		{
			Period duration = timeParser.parsePeriod(getArg("time"));
			DateTime expires = DateTime.now().plus(duration);
			String reason = StringUtils.join(args, " ", 2, args.length);
			RunsafePlayer victim = RunsafeServer.Instance.getPlayer(getArg("player"));
			if (victim == null)
				return "Player not found";

			if (victim instanceof RunsafeAmbiguousPlayer)
			{
				return String.format(
					"Multiple matches found: %s",
					StringUtils.join(((RunsafeAmbiguousPlayer) victim).getAmbiguity(), ", ")
				);
			}

			if (victim.hasPermission("runsafe.usercontrol.ban.immune"))
				return "You cannot ban that player";

			playerdb.setPlayerTemporaryBan(victim, expires);
			if (!victim.isOnline() || (executor != null && !executor.canSee(victim)))
			{
				victim.setBanned(true);
				logger.logKick(executor, victim, reason, true);
				playerdb.logPlayerBan(victim, executor, reason);
				return String.format("Temporarily banned offline player %s.", victim.getPrettyName());
			}
			if (lightning)
				victim.strikeWithLightning(fakeLightning);
			RunsafeServer.Instance.banPlayer(executor, victim, reason);
			return null;
		}
		catch (IllegalArgumentException e)
		{
			return "Unrecognized time format, use y/w/d/h/m/s";
		}
	}

	@Override
	public void OnConfigurationChanged(IConfiguration configuration)
	{
		banMessage = configuration.getConfigValueAsString("messages.tempban");
		lightning = configuration.getConfigValueAsBoolean("ban.lightning.strike");
		fakeLightning = !configuration.getConfigValueAsBoolean("ban.lightning.real");
	}

	String banMessage = "%3$s";
	private final PeriodFormatter timeParser;
	private final PlayerDatabase playerdb;
	private final PlayerKickLog logger;
	private boolean lightning;
	private boolean fakeLightning;
}
