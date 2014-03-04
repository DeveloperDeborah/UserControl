package no.runsafe.UserControl.command;

import no.runsafe.UserControl.database.PlayerDatabase;
import no.runsafe.UserControl.database.PlayerKickLog;
import no.runsafe.framework.api.IConfiguration;
import no.runsafe.framework.api.IServer;
import no.runsafe.framework.api.command.ExecutableCommand;
import no.runsafe.framework.api.command.ICommandExecutor;
import no.runsafe.framework.api.command.argument.IArgumentList;
import no.runsafe.framework.api.command.argument.Player;
import no.runsafe.framework.api.command.argument.TrailingArgument;
import no.runsafe.framework.api.event.plugin.IConfigurationChanged;
import no.runsafe.framework.api.player.IPlayer;

public class Ban extends ExecutableCommand implements IConfigurationChanged
{
	public Ban(PlayerKickLog log, PlayerDatabase playerDatabase, IServer server)
	{
		super("ban", "Permanently bans a player from the server", "runsafe.usercontrol.ban", new Player().require(), new TrailingArgument("reason"));
		logger = log;
		playerdb = playerDatabase;
		this.server = server;
	}

	@Override
	public String OnExecute(ICommandExecutor executor, IArgumentList parameters)
	{
		String reason = parameters.get("reason");

		IPlayer victim = parameters.getValue("player");
		if (victim == null)
			return null;

		if (victim.hasPermission("runsafe.usercontrol.ban.immune"))
			return "You cannot ban that player";

		IPlayer banningPlayer = null;
		if (executor instanceof IPlayer)
			banningPlayer = (IPlayer) executor;

		if (!victim.isOnline() || (banningPlayer != null && banningPlayer.shouldNotSee(victim)))
		{
			victim.setBanned(true);
			playerdb.logPlayerBan(victim, banningPlayer, reason);
			logger.logKick(banningPlayer, victim, reason, true);
			return String.format("Banned offline player %s.", victim.getPrettyName());
		}
		if (lightning)
			victim.strikeWithLightning(fakeLightning);
		server.banPlayer(banningPlayer, victim, reason);
		this.sendBanMessage(victim, banningPlayer, reason);
		return null;
	}

	private void sendBanMessage(IPlayer victim, IPlayer player, String reason)
	{
		if (player != null)
			server.broadcastMessage(String.format(this.onBanMessage, victim.getPrettyName(), reason, player.getPrettyName()));
		else
			server.broadcastMessage(String.format(this.onServerBanMessage, victim.getPrettyName(), reason));
	}

	@Override
	public void OnConfigurationChanged(IConfiguration configuration)
	{
		lightning = configuration.getConfigValueAsBoolean("ban.lightning.strike");
		fakeLightning = !configuration.getConfigValueAsBoolean("ban.lightning.real");
		this.onBanMessage = configuration.getConfigValueAsString("messages.onBan");
		this.onServerBanMessage = configuration.getConfigValueAsString("messages.onServerBan");
	}

	private final PlayerKickLog logger;
	private final PlayerDatabase playerdb;
	private final IServer server;
	private boolean lightning;
	private boolean fakeLightning;
	private String onBanMessage;
	private String onServerBanMessage;
}
