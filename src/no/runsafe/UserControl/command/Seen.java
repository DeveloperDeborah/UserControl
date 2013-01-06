package no.runsafe.UserControl.command;

import no.runsafe.UserControl.database.PlayerData;
import no.runsafe.UserControl.database.PlayerDatabase;
import no.runsafe.framework.command.RunsafeAsyncCommand;
import no.runsafe.framework.server.RunsafeServer;
import no.runsafe.framework.server.player.RunsafeAmbiguousPlayer;
import no.runsafe.framework.server.player.RunsafePlayer;
import no.runsafe.framework.timer.IScheduler;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.joda.time.format.PeriodFormat;

public class Seen extends RunsafeAsyncCommand
{
	public Seen(IScheduler scheduler, PlayerDatabase database)
	{
		super("seen", scheduler, "player");
		playerDatabase = database;
	}

	@Override
	public String requiredPermission()
	{
		return "runsafe.usercontrol.seen";
	}

	@Override
	public String OnExecute(RunsafePlayer executor, String[] args)
	{
		RunsafePlayer player = RunsafeServer.Instance.getPlayer(getArg("player"));
		if (player == null)
			return String.format("No players found matching %s", getArg("player"));

		if (player instanceof RunsafeAmbiguousPlayer)
		{
			return String.format(
				"Multiple matches found: %s",
				StringUtils.join(((RunsafeAmbiguousPlayer) player).getAmbiguity(), ", ")
			);
		}

		PlayerData data = playerDatabase.getData(player);
		if (player.isOnline() && (executor == null || executor.canSee(player)))
			return String.format(
				"Player %s is %sonline%s since %s",
				player.getPrettyName(),
				ChatColor.GREEN, ChatColor.RESET,
				formatTime(data.getLogin())
			);

		return String.format(
			"Player %s is %soffline%s since %s",
			player.getPrettyName(), ChatColor.RED, ChatColor.RESET,
			formatTime(player.isOnline() ? data.getLogin() : data.getLogout())
		);
	}

	private String formatTime(DateTime time)
	{
		if (time == null)
			return "null";

		Period period = new Period(time, DateTime.now(), output_format);
		return PeriodFormat.getDefault().print(period);
	}

	private final PlayerDatabase playerDatabase;
	private final PeriodType output_format = PeriodType.standard().withMillisRemoved().withSecondsRemoved();
}
