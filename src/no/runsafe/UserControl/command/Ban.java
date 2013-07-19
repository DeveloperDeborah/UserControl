package no.runsafe.UserControl.command;

import no.runsafe.UserControl.database.PlayerDatabase;
import no.runsafe.UserControl.database.PlayerKickLog;
import no.runsafe.framework.api.IConfiguration;
import no.runsafe.framework.api.command.ExecutableCommand;
import no.runsafe.framework.api.command.ICommandExecutor;
import no.runsafe.framework.api.event.plugin.IConfigurationChanged;
import no.runsafe.framework.minecraft.RunsafeServer;
import no.runsafe.framework.minecraft.player.RunsafeAmbiguousPlayer;
import no.runsafe.framework.minecraft.player.RunsafePlayer;

import java.util.Map;

public class Ban extends ExecutableCommand implements IConfigurationChanged
{
	public Ban(PlayerKickLog log, PlayerDatabase playerDatabase)
	{
		super("ban", "Permanently bans a player from the server", "runsafe.usercontrol.ban", "player", "reason");
		captureTail();
		logger = log;
		playerdb = playerDatabase;
	}

	@Override
	public String OnExecute(ICommandExecutor executor, Map<String, String> parameters)
	{
		String reason = parameters.get("reason");

		RunsafePlayer victim = RunsafeServer.Instance.getPlayer(parameters.get("player"));
		if (victim == null)
			return "Player not found";

		if (victim instanceof RunsafeAmbiguousPlayer)
			return victim.toString();

		if (victim.hasPermission("runsafe.usercontrol.ban.immune"))
			return "You cannot ban that player";

		RunsafePlayer banningPlayer = null;
		if (executor instanceof RunsafePlayer)
			banningPlayer = (RunsafePlayer) executor;

		if (!victim.isOnline() || (banningPlayer != null && banningPlayer.shouldNotSee(victim)))
		{
			victim.setBanned(true);
			playerdb.logPlayerBan(victim, banningPlayer, reason);
			logger.logKick(banningPlayer, victim, reason, true);
			return String.format("Banned offline player %s.", victim.getPrettyName());
		}
		if (lightning)
			victim.strikeWithLightning(fakeLightning);
		RunsafeServer.Instance.banPlayer(banningPlayer, victim, reason);
		this.sendBanMessage(victim, banningPlayer, reason);
		return null;
	}

	private void sendBanMessage(RunsafePlayer victim, RunsafePlayer player, String reason)
	{
		if (player != null)
			RunsafeServer.Instance.broadcastMessage(String.format(this.onBanMessage, victim.getPrettyName(), reason, player.getPrettyName()));
		else
			RunsafeServer.Instance.broadcastMessage(String.format(this.onServerBanMessage, victim.getPrettyName(), reason));
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
	private boolean lightning;
	private boolean fakeLightning;
	private String onBanMessage;
	private String onServerBanMessage;
}
