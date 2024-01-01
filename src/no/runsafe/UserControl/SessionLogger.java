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
import no.runsafe.framework.api.player.IPlayer;
import no.runsafe.framework.api.server.IPlayerProvider;
import no.runsafe.framework.minecraft.event.player.RunsafePlayerJoinEvent;
import no.runsafe.framework.minecraft.event.player.RunsafePlayerKickEvent;
import no.runsafe.framework.minecraft.event.player.RunsafePlayerQuitEvent;

public class SessionLogger implements IPluginEnabled, IPluginDisabled, IPlayerJoinEvent, IPlayerQuitEvent, IPlayerKickEvent
{
	public SessionLogger(
		PlayerDatabase players,
		PlayerSessionLog sessions,
		PlayerKickLog kickLog,
		PlayerUsernameLog playerUsernameLog,
		IPlayerProvider playerProvider,
		IServer server
	)
	{
		playerdb = players;
		sessiondb = sessions;
		kicklogger = kickLog;
		this.playerUsernameLog = playerUsernameLog;
		this.playerProvider = playerProvider;
		this.server = server;
	}

	@Override
	public void OnPlayerJoinEvent(RunsafePlayerJoinEvent event)
	{
		IPlayer player = event.getPlayer();
		playerdb.logPlayerInfo(player);
		sessiondb.logSessionStart(player);
		playerUsernameLog.logPlayerLogin(player);
	}

	@Override
	public void OnPlayerQuit(RunsafePlayerQuitEvent event)
	{
		playerdb.logPlayerLogout(event.getPlayer());
		sessiondb.logSessionClosed(event.getPlayer(), event.getQuitMessage());
	}

	@Override
	public void OnPlayerKick(RunsafePlayerKickEvent event)
	{
		sessiondb.logSessionClosed(event.getPlayer(), event.getLeaveMessage());
		kicklogger.logKick(event.getKicker(), event.getPlayer(), event.getReason(), !event.getPlayer().isNotBanned());
		if (!event.getPlayer().isNotBanned())
			playerdb.logPlayerBan(event.getPlayer(), event.getKicker(), event.getReason());
	}

	@Override
	public void OnPluginEnabled()
	{
		sessiondb.closeAllSessions("Possible crash");
		for (IPlayer player : playerProvider.getOnlinePlayers())
		{
			playerdb.logPlayerInfo(player);
			sessiondb.logSessionStart(player);
		}
	}

	@Override
	public void OnPluginDisabled()
	{
		sessiondb.closeAllSessions("Shutting down");
		for (IPlayer player : server.getOnlinePlayers())
			playerdb.logPlayerLogout(player);
	}

	private final PlayerDatabase playerdb;
	private final PlayerSessionLog sessiondb;
	private final PlayerKickLog kicklogger;
	private final PlayerUsernameLog playerUsernameLog;
	private final IPlayerProvider playerProvider;
	private final IServer server;
}
