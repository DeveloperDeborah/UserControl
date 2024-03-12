package no.runsafe.UserControl;

import no.runsafe.UserControl.database.PlayerDatabase;
import no.runsafe.UserControl.database.PlayerKickLog;
import no.runsafe.UserControl.database.PlayerSessionLog;
import no.runsafe.UserControl.database.PlayerUsernameLog;
import no.runsafe.framework.api.IServer;
import no.runsafe.framework.api.event.player.IPlayerJoinEvent;
import no.runsafe.framework.api.event.player.IPlayerKickEvent;
import no.runsafe.framework.api.event.player.IPlayerQuitEvent;
import no.runsafe.framework.api.event.plugin.IPluginDisabled;
import no.runsafe.framework.api.event.plugin.IPluginEnabled;
import no.runsafe.framework.api.log.IConsole;
import no.runsafe.framework.api.player.IPlayer;
import no.runsafe.framework.api.server.IPlayerProvider;
import no.runsafe.framework.minecraft.event.player.RunsafePlayerJoinEvent;
import no.runsafe.framework.minecraft.event.player.RunsafePlayerKickEvent;
import no.runsafe.framework.minecraft.event.player.RunsafePlayerQuitEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SessionLogger implements IPluginEnabled, IPluginDisabled, IPlayerJoinEvent, IPlayerQuitEvent, IPlayerKickEvent
{
	public SessionLogger(
		PlayerDatabase players,
		PlayerSessionLog sessions,
		PlayerKickLog kickLog,
		PlayerUsernameLog playerUsernameLog,
		IPlayerProvider playerProvider,
		IServer server,
		IConsole console
	)
	{
		playerDb = players;
		sessionDb = sessions;
		kickLogger = kickLog;
		this.playerUsernameLog = playerUsernameLog;
		this.playerProvider = playerProvider;
		this.server = server;
		this.console = console;
	}

	@Override
	public void OnPlayerJoinEvent(RunsafePlayerJoinEvent event)
	{
		IPlayer player = event.getPlayer();
		playerDb.logPlayerInfo(player);
		sessionDb.logSessionStart(player);
		playerUsernameLog.logPlayerLogin(player);

		Map<IPlayer, List<String>> alts = sessionDb.findAlternateAccounts(player);
		List<String> altNames = new ArrayList<>();
		if (!alts.isEmpty())
		{
			for (IPlayer alt : alts.keySet())
			{
				altNames.add(String.format("%s: &a%s&r", alt.getPrettyName(), String.join("%r,%a", alts.get(alt))));
			}
		}
		String message = altNames.isEmpty()
			? String.format("Player %s does not have any apparent alts", player.getPrettyName())
			: String.format("Player %s may have alts: %s", player.getPrettyName(), String.join(", ", altNames));

		console.logInformation(message + " ");
		List<IPlayer> players = server.getPlayersWithPermission("runsafe.usercontrol.alts");
		for(IPlayer online : players)
		{
			if (online.shouldNotSee(player))
			{
				continue;
			}
			online.sendColouredMessage(message);
		}
	}

	@Override
	public void OnPlayerQuit(RunsafePlayerQuitEvent event)
	{
		playerDb.logPlayerLogout(event.getPlayer());
		sessionDb.logSessionClosed(event.getPlayer(), event.getQuitMessage());
	}

	@Override
	public void OnPlayerKick(RunsafePlayerKickEvent event)
	{
		sessionDb.logSessionClosed(event.getPlayer(), event.getLeaveMessage());
		kickLogger.logKick(event.getKicker(), event.getPlayer(), event.getReason(), !event.getPlayer().isNotBanned());
		if (!event.getPlayer().isNotBanned())
			playerDb.logPlayerBan(event.getPlayer(), event.getKicker(), event.getReason());
	}

	@Override
	public void OnPluginEnabled()
	{
		sessionDb.closeAllSessions("Possible crash");
		for (IPlayer player : playerProvider.getOnlinePlayers())
		{
			playerDb.logPlayerInfo(player);
			sessionDb.logSessionStart(player);
		}
	}

	@Override
	public void OnPluginDisabled()
	{
		sessionDb.closeAllSessions("Shutting down");
		for (IPlayer player : server.getOnlinePlayers())
			playerDb.logPlayerLogout(player);
	}

	private final PlayerDatabase playerDb;
	private final PlayerSessionLog sessionDb;
	private final PlayerKickLog kickLogger;
	private final PlayerUsernameLog playerUsernameLog;
	private final IPlayerProvider playerProvider;
	private final IServer server;
	private final IConsole console;
}
