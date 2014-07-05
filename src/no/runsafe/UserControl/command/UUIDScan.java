package no.runsafe.UserControl.command;

import no.runsafe.UserControl.database.PlayerDatabase;
import no.runsafe.framework.api.command.argument.IArgumentList;
import no.runsafe.framework.api.command.console.ConsoleCommand;

public class UUIDScan extends ConsoleCommand
{
	public UUIDScan(PlayerDatabase players)
	{
		super("uuidscan", "Force an UUID scan on the player database");
		this.players = players;
	}

	@Override
	public String OnExecute(IArgumentList parameters)
	{
		players.updateUUIDs();
		return null;
	}

	private final PlayerDatabase players;
}
