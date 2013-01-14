package no.runsafe.UserControl;

import no.runsafe.UserControl.database.PlayerDatabase;
import no.runsafe.UserControl.database.PlayerKickLog;
import no.runsafe.UserControl.database.PlayerSessionLog;
import no.runsafe.framework.event.IPluginDisabled;
import no.runsafe.framework.event.IPluginEnabled;
import no.runsafe.framework.event.player.IPlayerJoinEvent;
import no.runsafe.framework.event.player.IPlayerKickEvent;
import no.runsafe.framework.event.player.IPlayerQuitEvent;
import no.runsafe.framework.server.RunsafeServer;
import no.runsafe.framework.server.event.player.RunsafePlayerJoinEvent;
import no.runsafe.framework.server.event.player.RunsafePlayerKickEvent;
import no.runsafe.framework.server.event.player.RunsafePlayerQuitEvent;
import no.runsafe.framework.server.player.RunsafePlayer;

public class SessionLogger implements IPluginEnabled, IPluginDisabled, IPlayerJoinEvent, IPlayerQuitEvent, IPlayerKickEvent
{
	public SessionLogger(PlayerDatabase players, PlayerSessionLog sessions, PlayerKickLog kickLog)
	{
		playerdb = players;
		sessiondb = sessions;
		kicklogger = kickLog;
	}

	@Override
	public void OnPlayerJoinEvent(RunsafePlayerJoinEvent event)
	{
		playerdb.logPlayerInfo(event.getPlayer());
		sessiondb.logSessionStart(event.getPlayer());
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
		kicklogger.logKick(event.getKicker(), event.getPlayer(), event.getReason(), event.getPlayer().isBanned());
		if (event.getPlayer().isBanned())
			playerdb.logPlayerBan(event.getPlayer(), event.getKicker(), event.getReason());
	}

	@Override
	public void OnPluginEnabled()
	{
		sessiondb.closeAllSessions("Possible crash");
		for (RunsafePlayer player : RunsafeServer.Instance.getOnlinePlayers())
		{
			playerdb.logPlayerInfo(player);
			sessiondb.logSessionStart(player);
		}
	}

	@Override
	public void OnPluginDisabled()
	{
		sessiondb.closeAllSessions("Shutting down");
	}

	private final PlayerDatabase playerdb;
	private final PlayerSessionLog sessiondb;
	private final PlayerKickLog kicklogger;
}
