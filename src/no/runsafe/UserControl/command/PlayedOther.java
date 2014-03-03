package no.runsafe.UserControl.command;

import no.runsafe.UserControl.database.PlayerSessionLog;
import no.runsafe.framework.api.IScheduler;
import no.runsafe.framework.api.command.IBranchingExecution;
import no.runsafe.framework.api.command.argument.Player;
import no.runsafe.framework.api.command.argument.IArgumentList;
import no.runsafe.framework.api.command.player.PlayerAsyncCommand;
import no.runsafe.framework.api.player.IPlayer;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.joda.time.format.PeriodFormat;

public class PlayedOther extends PlayerAsyncCommand implements IBranchingExecution
{
	public PlayedOther(IScheduler scheduler, PlayerSessionLog database)
	{
		super("played", "Tells you how much time someone has spent on the server", "runsafe.usercontrol.played", scheduler, new Player.Any.Required());
		this.database = database;
	}

	@Override
	public String OnAsyncExecute(IPlayer executor, IArgumentList parameters)
	{
		IPlayer player = parameters.getValue("player");
		if (player == null)
			return "&cPlayer not found!";

		Duration played = database.GetTimePlayed(player);
		return String.format("%s has played for &6%s", player.getPrettyName(), formatTime(played));
	}

	private String formatTime(Duration time)
	{
		if (time == null)
			return "null";

		Period period = new Period(time, DateTime.now(), output_format);
		return PeriodFormat.getDefault().print(period);
	}

	private final PlayerSessionLog database;
	private final PeriodType output_format = PeriodType.standard().withMillisRemoved().withSecondsRemoved();
}
