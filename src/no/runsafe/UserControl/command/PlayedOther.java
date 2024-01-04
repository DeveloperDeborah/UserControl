package no.runsafe.UserControl.command;

import no.runsafe.UserControl.TimeFormatter;
import no.runsafe.UserControl.database.PlayerSessionLog;
import no.runsafe.framework.api.IScheduler;
import no.runsafe.framework.api.command.AsyncCommand;
import no.runsafe.framework.api.command.IBranchingExecution;
import no.runsafe.framework.api.command.ICommandExecutor;
import no.runsafe.framework.api.command.argument.Player;
import no.runsafe.framework.api.command.argument.IArgumentList;
import no.runsafe.framework.api.player.IPlayer;

import java.time.Duration;

public class PlayedOther extends AsyncCommand implements IBranchingExecution
{
	public PlayedOther(PlayerSessionLog database, IScheduler scheduler)
	{
		super("played", "Tells you how much time someone has spent on the server", "runsafe.usercontrol.played", scheduler, new Player().require());
		this.database = database;
	}

	@Override
	public String OnAsyncExecute(ICommandExecutor executor, IArgumentList parameters)
	{
		IPlayer player = parameters.getValue("player");
		if (player == null)
			return "&cPlayer not found!";

		Duration played = database.GetTimePlayed(player);
		return String.format("%s has played for &6%s", player.getPrettyName(), TimeFormatter.formatDuration(played));
	}

	private final PlayerSessionLog database;
}
