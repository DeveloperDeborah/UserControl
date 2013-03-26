package no.runsafe.UserControl.command;

import no.runsafe.UserControl.database.PlayerData;
import no.runsafe.UserControl.database.PlayerDatabase;
import no.runsafe.framework.command.AsyncCommand;
import no.runsafe.framework.server.ICommandExecutor;
import no.runsafe.framework.server.RunsafeServer;
import no.runsafe.framework.server.player.RunsafeAmbiguousPlayer;
import no.runsafe.framework.server.player.RunsafePlayer;
import no.runsafe.framework.timer.IScheduler;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.joda.time.format.PeriodFormat;

import java.util.HashMap;

public class Seen extends AsyncCommand
{
	public Seen(IScheduler scheduler, PlayerDatabase database)
	{
		super("seen", "Check when a player was last on the server", "runsafe.usercontrol.seen", scheduler, "player");
		playerDatabase = database;
	}

	public String OnAsyncExecute(ICommandExecutor executor, HashMap<String, String> parameters)
	{
		String playerName = parameters.get("player");
		RunsafePlayer player = RunsafeServer.Instance.getPlayer(playerName);
		if (player == null)
			return String.format("No players found matching %s", playerName);

		if (player instanceof RunsafeAmbiguousPlayer)
		{
			return String.format(
				"Multiple matches found: %s",
				StringUtils.join(((RunsafeAmbiguousPlayer) player).getAmbiguity(), ", ")
			);
		}

		RunsafePlayer checker = null;
		if (executor instanceof RunsafePlayer)
			checker = (RunsafePlayer) executor;

		PlayerData data = playerDatabase.getData(player);

		if (data != null && data.getBanned() != null)
			return String.format(
				"Player %s has been &4banned&r since %s",
				player.getPrettyName(),
				formatTime(data.getBanned())
			);

		if (player.isOnline() && (checker == null || checker.canSee(player)))
			return String.format(
				"Player %s is &aonline&r since %s",
				player.getPrettyName(),
				formatTime(data.getLogin())
			);

		return String.format(
			"Player %s is &coffline&r since %s",
			player.getPrettyName(),
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
