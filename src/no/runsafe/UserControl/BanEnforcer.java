package no.runsafe.UserControl;

import no.runsafe.UserControl.database.PlayerData;
import no.runsafe.UserControl.database.PlayerDatabase;
import no.runsafe.framework.api.IConfiguration;
import no.runsafe.framework.api.event.player.IPlayerPreLoginEvent;
import no.runsafe.framework.api.event.plugin.IConfigurationChanged;
import no.runsafe.framework.api.player.IPlayer;
import no.runsafe.framework.api.server.IPlayerManager;
import no.runsafe.framework.minecraft.event.player.RunsafePlayerPreLoginEvent;
import org.apache.commons.lang.time.DurationFormatUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

public class BanEnforcer implements IPlayerPreLoginEvent, IConfigurationChanged
{
	public BanEnforcer(PlayerDatabase playerDatabase, IPlayerManager playerManager)
	{
		playerDb = playerDatabase;
		this.playerManager = playerManager;
	}

	@Override
	public void OnBeforePlayerLogin(RunsafePlayerPreLoginEvent event)
	{
		if(event.getPlayer() == null || event.getPlayer().getName() == null)
		{
			event.playerBanned("Invalid username");
			return;
		}

		if (activeBans.containsKey(event.getPlayer()))
		{
			event.playerBanned(activeBans.get(event.getPlayer()));
			return;
		}

		IPlayer player = event.getPlayer();

		if (player == null)
			return;

		PlayerData data = playerDb.getData(player);
		if (data == null || data.getBanned() == null)
			return;

		Instant unban = data.getUnban();
		if (unban == null)
		{
			String banReason = String.format(banMessageFormat, data.getBanReason());
			event.playerBanned(banReason);
			activeBans.put(event.getPlayer(), banReason);
			return;
		}

		if (!unban.isAfter(Instant.now()))
		{
			playerDb.logPlayerUnban(event.getPlayer());
			playerManager.unbanPlayer(player);
			return;
		}

		Duration left = Duration.between(Instant.now(), unban);
		event.playerBanned(String.format(
			tempBanMessageFormat,
			data.getBanReason(),
			DurationFormatUtils.formatDurationWords(left.toMillis(), true, true)
		));
	}

	@Override
	public void OnConfigurationChanged(IConfiguration configuration)
	{
		banMessageFormat = configuration.getConfigValueAsString("messages.ban");
		tempBanMessageFormat = configuration.getConfigValueAsString("messages.tempban");
	}

	public void flushCache()
	{
		activeBans.clear();
	}

	private final PlayerDatabase playerDb;
	private final IPlayerManager playerManager;
	private String banMessageFormat = "Banned: %s";
	private String tempBanMessageFormat = "Temporarily banned: %s [expires in %s]";
	private final ConcurrentHashMap<IPlayer, String> activeBans = new ConcurrentHashMap<>();
}
