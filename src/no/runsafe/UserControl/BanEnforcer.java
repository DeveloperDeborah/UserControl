package no.runsafe.UserControl;

import no.runsafe.UserControl.database.PlayerData;
import no.runsafe.UserControl.database.PlayerDatabase;
import no.runsafe.framework.configuration.IConfiguration;
import no.runsafe.framework.event.IConfigurationChanged;
import no.runsafe.framework.event.player.IPlayerPreLoginEvent;
import no.runsafe.framework.server.event.player.RunsafePlayerPreLoginEvent;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.joda.time.format.PeriodFormat;

import java.util.concurrent.ConcurrentHashMap;

public class BanEnforcer implements IPlayerPreLoginEvent, IConfigurationChanged
{
	public BanEnforcer(PlayerDatabase playerDatabase)
	{
		playerdb = playerDatabase;
	}

	@Override
	public void OnBeforePlayerLogin(RunsafePlayerPreLoginEvent event)
	{
		if (activeBans.containsKey(event.getName()))
		{
			event.playerBanned(activeBans.get(event.getName()));
			return;
		}
		PlayerData data = playerdb.getData(event.getPlayer());
		if (data.getBanned() != null)
		{
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
					event.getPlayer().setBanned(false);
				}
				return;
			}
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

	private final PlayerDatabase playerdb;
	private String banMessageFormat = "Banned: %s";
	private String tempBanMessageFormat = "Temporarily banned: %s [expires in %s]";
	private final ConcurrentHashMap<String, String> activeBans = new ConcurrentHashMap<String, String>();
	private static final PeriodType LONG_TIME_LEFT = PeriodType.standard().withMillisRemoved().withSecondsRemoved();
	private static final PeriodType SHORT_TIME_LEFT = PeriodType.standard().withMillisRemoved();
	private static final Duration ONE_MINUTE = Duration.standardMinutes(1);
}
