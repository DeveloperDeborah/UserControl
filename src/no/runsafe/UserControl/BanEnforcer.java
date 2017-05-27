package no.runsafe.UserControl;

import no.runsafe.UserControl.database.PlayerData;
import no.runsafe.UserControl.database.PlayerDatabase;
import no.runsafe.framework.api.IConfiguration;
import no.runsafe.framework.api.IServer;
import no.runsafe.framework.api.event.player.IPlayerPreLoginEvent;
import no.runsafe.framework.api.event.plugin.IConfigurationChanged;
import no.runsafe.framework.api.player.IPlayer;
import no.runsafe.framework.minecraft.event.player.RunsafePlayerPreLoginEvent;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.PeriodType;
import org.joda.time.format.PeriodFormat;

import java.util.concurrent.ConcurrentHashMap;

public class BanEnforcer implements IPlayerPreLoginEvent, IConfigurationChanged
{
	public BanEnforcer(PlayerDatabase playerDatabase, IServer server)
	{
		playerdb = playerDatabase;
		this.server = server;
	}

	@Override
	public void OnBeforePlayerLogin(RunsafePlayerPreLoginEvent event)
	{
		if(event.getPlayer() == null || event.getPlayer().getName() == null)
		{
			event.playerBanned("Invalid username");
			return;
		}

		if (activeBans.containsKey(event.getName()))
		{
			event.playerBanned(activeBans.get(event.getName()));
			return;
		}

		IPlayer player = event.getPlayer();

		if (player == null)
			return;

		PlayerData data = playerdb.getData(player);
		if (data == null || data.getBanned() == null)
			return;

		DateTime unban = data.getUnban();
		if (unban != null)
		{
			if (unban.isAfter(DateTime.now()))
			{
				Duration left = new Duration(DateTime.now(), unban);
				event.playerBanned(
					String.format(
						tempBanMessageFormat,
						data.getBanReason(),
						PeriodFormat.getDefault().print(
							left.toPeriod(ONE_MINUTE.isLongerThan(left) ? SHORT_TIME_LEFT : LONG_TIME_LEFT)
						)
					)
				);
			}
			else
			{
				playerdb.logPlayerUnban(event.getPlayer());
				server.unbanPlayer(player);
			}
		}
		else
		{
			String banReason = String.format(banMessageFormat, data.getBanReason());
			event.playerBanned(banReason);
			activeBans.put(event.getName(), banReason);
		}
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

	private final PlayerDatabase playerdb;
	private final IServer server;
	private String banMessageFormat = "Banned: %s";
	private String tempBanMessageFormat = "Temporarily banned: %s [expires in %s]";
	private final ConcurrentHashMap<String, String> activeBans = new ConcurrentHashMap<String, String>();
	private static final PeriodType LONG_TIME_LEFT = PeriodType.standard().withMillisRemoved().withSecondsRemoved();
	private static final PeriodType SHORT_TIME_LEFT = PeriodType.standard().withMillisRemoved();
	private static final Duration ONE_MINUTE = Duration.standardMinutes(1);
}
