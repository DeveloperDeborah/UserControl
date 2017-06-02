package no.runsafe.UserControl.command;

import no.runsafe.framework.api.IConfiguration;
import no.runsafe.framework.api.command.ExecutableCommand;
import no.runsafe.framework.api.command.ICommandExecutor;
import no.runsafe.framework.api.command.argument.IArgumentList;
import no.runsafe.framework.api.command.argument.Player;
import no.runsafe.framework.api.command.argument.TrailingArgument;
import no.runsafe.framework.api.event.plugin.IConfigurationChanged;
import no.runsafe.framework.api.player.IPlayer;
import no.runsafe.framework.api.server.IBroadcast;
import no.runsafe.framework.api.server.IPlayerManager;

public class Kick extends ExecutableCommand implements IConfigurationChanged
{
	public Kick(IBroadcast broadcaster, IPlayerManager playerManager)
	{
		super("kick", "Kicks a player from the server", "runsafe.usercontrol.kick", new Player().onlineOnly().require(), new TrailingArgument("reason"));
		this.broadcaster = broadcaster;
		this.playerManager = playerManager;
	}

	@Override
	public String OnExecute(ICommandExecutor executor, IArgumentList parameters)
	{
		IPlayer victim = parameters.getValue("player");
		if (victim == null)
			return null;

		if (victim.hasPermission("runsafe.usercontrol.kick.immune"))
			return "You cannot kick that player";

		String reason = parameters.getValue("reason");

		if (executor instanceof IPlayer)
		{
			IPlayer executorPlayer = (IPlayer) executor;
			playerManager.kickPlayer(executorPlayer, victim, reason);
			this.sendKickMessage(victim, executorPlayer, reason);
		}
		else
		{
			playerManager.kickPlayer(null, victim, reason);
			this.sendKickMessage(victim, null, reason);
		}

		return null;
	}

	@Override
	public void OnConfigurationChanged(IConfiguration configuration)
	{
		this.onKickMessage = configuration.getConfigValueAsString("messages.onKick");
		this.onServerKickMessage = configuration.getConfigValueAsString("messages.onServerKick");
	}

	private void sendKickMessage(IPlayer victim, IPlayer player, String reason)
	{
		if (player != null)
			broadcaster.broadcastMessage(String.format(this.onKickMessage, victim.getPrettyName(), reason, player.getPrettyName()));
		else
			broadcaster.broadcastMessage(String.format(this.onServerKickMessage, victim.getPrettyName(), reason));
	}

	private final IBroadcast broadcaster;
	private final IPlayerManager playerManager;
	private String onKickMessage;
	private String onServerKickMessage;
}
