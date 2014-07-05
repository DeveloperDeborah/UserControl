package no.runsafe.UserControl.command;

import no.runsafe.UserControl.database.PlayerDatabase;
import no.runsafe.framework.api.IScheduler;
import no.runsafe.framework.api.command.argument.IArgumentList;
import no.runsafe.framework.api.command.console.ConsoleAsyncCommand;

public class UUIDScan extends ConsoleAsyncCommand
{
	public UUIDScan(PlayerDatabase players, IScheduler scheduler)
	{
		super("uuidscan", "Force an UUID scan on the player database", scheduler);
		this.players = players;
	}

	@Override
	public String OnAsyncExecute(IArgumentList parameters)
	{
		players.updateUUIDs();
		return null;
	}

	private final PlayerDatabase players;

}
