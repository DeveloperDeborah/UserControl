package no.runsafe.UserControl;

import no.runsafe.UserControl.database.PlayerData;
import no.runsafe.UserControl.database.PlayerDatabase;
import no.runsafe.framework.hook.IPlayerSeen;
import no.runsafe.framework.server.player.RunsafePlayer;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.joda.time.format.PeriodFormat;

public class PlayerHandler implements IPlayerSeen
{
	public PlayerHandler(PlayerDatabase database)
	{
		this.database = database;
	}

	@Override
	public String GetLastSeen(RunsafePlayer player, RunsafePlayer checker)
	{
		return this.lastSeenPlayer(player, checker);
	}

	public String lastSeenPlayer(RunsafePlayer player, RunsafePlayer checker)
	{
		PlayerData data = database.getData(player);
		if (data.getBanned() != null)
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

	private final PlayerDatabase database;
	private final PeriodType output_format = PeriodType.standard().withMillisRemoved().withSecondsRemoved();
}
