package no.runsafe.UserControl;

import no.runsafe.UserControl.database.PlayerDatabase;
import no.runsafe.UserControl.database.PlayerSessionLog;
import no.runsafe.framework.event.IPluginEnabled;
import no.runsafe.framework.event.player.IPlayerJoinEvent;
import no.runsafe.framework.event.player.IPlayerQuitEvent;
import no.runsafe.framework.server.RunsafeServer;
import no.runsafe.framework.server.event.player.RunsafePlayerJoinEvent;
import no.runsafe.framework.server.event.player.RunsafePlayerQuitEvent;
import no.runsafe.framework.server.player.RunsafePlayer;

public class SessionLogger implements IPluginEnabled, IPlayerJoinEvent, IPlayerQuitEvent
{
	public SessionLogger(PlayerDatabase players, PlayerSessionLog sessions)
	{
		playerdb = players;
		sessiondb = sessions;
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
	public void OnPluginEnabled()
	{
		sessiondb.closeAllSessions("Possible crash");
		for(RunsafePlayer player : RunsafeServer.Instance.getOnlinePlayers())
		{
			playerdb.logPlayerInfo(player);
			sessiondb.logSessionStart(player);
		}
	}

	private PlayerDatabase playerdb;
	private PlayerSessionLog sessiondb;
}
