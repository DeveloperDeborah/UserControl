package no.runsafe.UserControl.command;

import no.runsafe.UserControl.TimeFormatter;
import no.runsafe.UserControl.database.PlayerData;
import no.runsafe.UserControl.database.PlayerDatabase;
import no.runsafe.framework.api.IScheduler;
import no.runsafe.framework.api.command.AsyncCommand;
import no.runsafe.framework.api.command.ICommandExecutor;
import no.runsafe.framework.api.command.argument.IArgumentList;
import no.runsafe.framework.api.command.argument.Player;
import no.runsafe.framework.api.player.IPlayer;

public class Seen extends AsyncCommand
{
	public Seen(IScheduler scheduler, PlayerDatabase database)
	{
		super("seen", "Check when a player was last on the server", "runsafe.usercontrol.seen", scheduler, new Player().require());
		playerDatabase = database;
	}

	public String OnAsyncExecute(ICommandExecutor executor, IArgumentList parameters)
	{
		IPlayer player = parameters.getValue("player");
		if (player == null)
			return null;

		IPlayer checker = null;
		if (executor instanceof IPlayer)
			checker = (IPlayer) executor;

		PlayerData data = playerDatabase.getData(player);

		if (data == null)
			return String.format("No data for %s found..", player.getPrettyName());

		if (data.getBanned() != null)
			return String.format(
				"Player %s has been &4banned&r since %s",
				player.getPrettyName(),
				TimeFormatter.formatInstant(data.getBanned())
			);

		if (player.isOnline() && (checker == null || !checker.shouldNotSee(player)))
			return String.format(
				"Player %s is &aonline&r since %s",
				player.getPrettyName(),
				TimeFormatter.formatInstant(data.getLogin())
			);

		return String.format(
			"Player %s is &coffline&r since %s",
			player.getPrettyName(),
			TimeFormatter.formatInstant(player.isOnline() ? data.getLogin() : data.getLogout())
		);
	}

	private final PlayerDatabase playerDatabase;
}
