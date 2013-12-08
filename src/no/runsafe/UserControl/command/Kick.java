package no.runsafe.UserControl.command;

import no.runsafe.framework.api.IConfiguration;
import no.runsafe.framework.api.IServer;
import no.runsafe.framework.api.command.ExecutableCommand;
import no.runsafe.framework.api.command.ICommandExecutor;
import no.runsafe.framework.api.command.argument.PlayerArgument;
import no.runsafe.framework.api.command.argument.TrailingArgument;
import no.runsafe.framework.api.event.plugin.IConfigurationChanged;
import no.runsafe.framework.api.player.IAmbiguousPlayer;
import no.runsafe.framework.api.player.IPlayer;

import java.util.Map;

public class Kick extends ExecutableCommand implements IConfigurationChanged
{
	public Kick(IServer server)
	{
		super("kick", "Kicks a player from the server", "runsafe.usercontrol.kick", new PlayerArgument(), new TrailingArgument("reason"));
		this.server = server;
	}

	@Override
	public String OnExecute(ICommandExecutor executor, Map<String, String> parameters)
	{
		IPlayer victim;
		if (executor instanceof IPlayer)
			victim = server.getOnlinePlayer((IPlayer) executor, parameters.get("player"));
		else
			victim = server.getOnlinePlayer(null, parameters.get("player"));
		if (victim == null)
			return "Player not found";

		if (victim instanceof IAmbiguousPlayer)
			return victim.toString();

		if (victim.hasPermission("runsafe.usercontrol.kick.immune"))
			return "You cannot kick that player";

		String reason = parameters.get("reason");

		if (executor instanceof IPlayer)
		{
			IPlayer executorPlayer = (IPlayer) executor;
			server.kickPlayer(executorPlayer, victim, reason);
			this.sendKickMessage(victim, executorPlayer, reason);
		}
		else
		{
			server.kickPlayer(null, victim, reason);
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
			server.broadcastMessage(String.format(this.onKickMessage, victim.getPrettyName(), reason, player.getPrettyName()));
		else
			server.broadcastMessage(String.format(this.onServerKickMessage, victim.getPrettyName(), reason));
	}

	private final IServer server;
	private String onKickMessage;
	private String onServerKickMessage;
}
