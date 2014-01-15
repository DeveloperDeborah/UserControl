package no.runsafe.UserControl.command;

import no.runsafe.UserControl.database.PlayerDatabase;
import no.runsafe.UserControl.database.PlayerKickLog;
import no.runsafe.framework.api.IConfiguration;
import no.runsafe.framework.api.IServer;
import no.runsafe.framework.api.command.ExecutableCommand;
import no.runsafe.framework.api.command.ICommandExecutor;
import no.runsafe.framework.api.command.argument.AnyPlayerRequired;
import no.runsafe.framework.api.command.argument.IArgumentList;
import no.runsafe.framework.api.command.argument.RequiredArgument;
import no.runsafe.framework.api.command.argument.TrailingArgument;
import no.runsafe.framework.api.event.plugin.IConfigurationChanged;
import no.runsafe.framework.api.player.IAmbiguousPlayer;
import no.runsafe.framework.api.player.IPlayer;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

public class TempBan extends ExecutableCommand implements IConfigurationChanged
{
	public TempBan(PlayerDatabase playerDatabase, PlayerKickLog logger, IServer server)
	{
		super(
			"tempban", "Temporarily ban a player from the server", "runsafe.usercontrol.ban.temporary",
			new AnyPlayerRequired(), new RequiredArgument("time"), new TrailingArgument("reason")
		);
		this.logger = logger;
		this.server = server;
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
	public String OnExecute(ICommandExecutor executor, IArgumentList parameters)
	{
		try
		{
			Period duration = timeParser.parsePeriod(parameters.get("time"));
			DateTime expires = DateTime.now().plus(duration);
			String reason = parameters.get("reason");

			IPlayer victim = server.getPlayer(parameters.get("player"));
			if (victim == null)
				return "Player not found";

			if (victim.hasPermission("runsafe.usercontrol.ban.immune"))
				return "You cannot ban that player";

			playerdb.setPlayerTemporaryBan(victim, expires);

			IPlayer banner = null;
			if (executor instanceof IPlayer)
				banner = (IPlayer) executor;

			if (!victim.isOnline() || (banner != null && banner.shouldNotSee(victim)))
			{
				victim.setBanned(true);
				logger.logKick(banner, victim, reason, true);
				playerdb.logPlayerBan(victim, banner, reason);
				return String.format("Temporarily banned offline player %s.", victim.getPrettyName());
			}
			if (lightning)
				victim.strikeWithLightning(fakeLightning);
			server.banPlayer(banner, victim, reason);
			this.sendTempBanMessage(victim, executor, reason);
			return null;
		}
		catch (IllegalArgumentException e)
		{
			return "Unrecognized time format, use y/w/d/h/m/s";
		}
	}

	private void sendTempBanMessage(IPlayer victim, ICommandExecutor executor, String reason)
	{
		if (executor instanceof IPlayer)
			server.broadcastMessage(String.format(this.onTempBanMessage, victim.getPrettyName(), reason, ((IPlayer) executor).getPrettyName()));
		else
			server.broadcastMessage(String.format(this.onServerTempBanMessage, victim.getPrettyName(), reason));
	}

	@Override
	public void OnConfigurationChanged(IConfiguration configuration)
	{
		lightning = configuration.getConfigValueAsBoolean("ban.lightning.strike");
		fakeLightning = !configuration.getConfigValueAsBoolean("ban.lightning.real");
		this.onTempBanMessage = configuration.getConfigValueAsString("messages.onTempBan");
		this.onServerTempBanMessage = configuration.getConfigValueAsString("messages.onServerTempBan");
	}

	private final PeriodFormatter timeParser;
	private final PlayerDatabase playerdb;
	private final PlayerKickLog logger;
	private final IServer server;
	private boolean lightning;
	private boolean fakeLightning;
	private String onTempBanMessage;
	private String onServerTempBanMessage;
}
