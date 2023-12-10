package no.runsafe.UserControl.command;

import no.runsafe.UserControl.database.PlayerDatabase;
import no.runsafe.UserControl.database.PlayerKickLog;
import no.runsafe.framework.api.IConfiguration;
import no.runsafe.framework.api.command.ExecutableCommand;
import no.runsafe.framework.api.command.ICommandExecutor;
import no.runsafe.framework.api.command.argument.*;
import no.runsafe.framework.api.event.plugin.IConfigurationChanged;
import no.runsafe.framework.api.player.IPlayer;
import no.runsafe.framework.api.server.IBroadcast;
import no.runsafe.framework.api.server.IPlayerManager;
import org.joda.time.DateTime;

public class TempBan extends ExecutableCommand implements IConfigurationChanged
{
	public TempBan(
		PlayerDatabase playerDatabase,
		PlayerKickLog logger,
		IBroadcast broadcaster,
		IPlayerManager playerManager
	)
	{
		super(
			"tempban", "Temporarily ban a player from the server", "runsafe.usercontrol.ban.temporary",
			new Player().require(), new Duration("time").require(), new TrailingArgument("reason")
		);
		this.logger = logger;
		this.broadcaster = broadcaster;
		this.playerManager = playerManager;
		playerdb = playerDatabase;
	}

	@Override
	public String OnExecute(ICommandExecutor executor, IArgumentList parameters)
	{
		java.time.Duration duration = parameters.getValue("time");
		if (duration == null)
			return null;
		DateTime expires = DateTime.now().plus(duration.toMillis());
		String reason = parameters.getValue("reason");

		IPlayer victim = parameters.getValue("player");
		if (victim == null)
			return null;

		if (victim.hasPermission("runsafe.usercontrol.ban.immune"))
			return "You cannot ban that player";

		playerdb.setPlayerTemporaryBan(victim, expires);

		IPlayer banningPlayer = null;
		if (executor instanceof IPlayer)
			banningPlayer = (IPlayer) executor;

		if (!victim.isOnline() || (banningPlayer != null && banningPlayer.shouldNotSee(victim)))
		{
			playerManager.banPlayer(banningPlayer, victim, reason);
			logger.logKick(banningPlayer, victim, reason, true);
			playerdb.logPlayerBan(victim, banningPlayer, reason);
			return String.format("Temporarily banned offline player %s.", victim.getPrettyName());
		}
		if (lightning)
			victim.strikeWithLightning(fakeLightning);
		playerManager.banPlayer(banningPlayer, victim, reason);
		this.sendTempBanMessage(victim, executor, reason);
		return null;
	}

	private void sendTempBanMessage(IPlayer victim, ICommandExecutor executor, String reason)
	{
		if (executor instanceof IPlayer)
			broadcaster.broadcastMessage(String.format(this.onTempBanMessage, victim.getPrettyName(), reason, ((IPlayer) executor).getPrettyName()));
		else
			broadcaster.broadcastMessage(String.format(this.onServerTempBanMessage, victim.getPrettyName(), reason));
	}

	@Override
	public void OnConfigurationChanged(IConfiguration configuration)
	{
		lightning = configuration.getConfigValueAsBoolean("ban.lightning.strike");
		fakeLightning = !configuration.getConfigValueAsBoolean("ban.lightning.real");
		this.onTempBanMessage = configuration.getConfigValueAsString("messages.onTempBan");
		this.onServerTempBanMessage = configuration.getConfigValueAsString("messages.onServerTempBan");
	}

	private final PlayerDatabase playerdb;
	private final PlayerKickLog logger;
	private final IBroadcast broadcaster;
	private final IPlayerManager playerManager;
	private boolean lightning;
	private boolean fakeLightning;
	private String onTempBanMessage;
	private String onServerTempBanMessage;
}
