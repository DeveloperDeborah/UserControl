package no.runsafe.UserControl.command;

import no.runsafe.UserControl.database.PlayerSessionLog;
import no.runsafe.framework.api.IScheduler;
import no.runsafe.framework.api.command.IBranchingExecution;
import no.runsafe.framework.api.command.argument.IArgumentList;
import no.runsafe.framework.api.command.player.PlayerAsyncCommand;
import no.runsafe.framework.api.player.IPlayer;
import org.apache.commons.lang.time.DurationFormatUtils;

import java.time.Duration;

public class Played extends PlayerAsyncCommand implements IBranchingExecution
{
	public Played(IScheduler scheduler, PlayerSessionLog database)
	{
		super("played", "Tells you how much time you have spent on the server", "runsafe.usercontrol.played", scheduler);
		this.database = database;
	}

	@Override
	public String OnAsyncExecute(IPlayer executor, IArgumentList parameters)
	{
		Duration played = database.GetTimePlayed(executor);
		return String.format("You have played &6%s", formatTime(played));
	}

	private String formatTime(Duration time)
	{
		if (time == null)
			return "null";

		return DurationFormatUtils.formatDurationWords(time.toMillis(), true, true);
	}

	private final PlayerSessionLog database;
}
