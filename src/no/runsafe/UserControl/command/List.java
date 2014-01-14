package no.runsafe.UserControl.command;

import no.runsafe.framework.api.IServer;
import no.runsafe.framework.api.command.ExecutableCommand;
import no.runsafe.framework.api.command.ICommandExecutor;
import no.runsafe.framework.api.command.argument.IArgumentList;
import no.runsafe.framework.api.player.IPlayer;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;

public class List extends ExecutableCommand
{
	public List(IServer server)
	{
		super("list", "List players connected to the server", "runsafe.usercontrol.list");
		this.server = server;
	}

	@Override
	public String OnExecute(ICommandExecutor executor, IArgumentList parameters)
	{
		ArrayList<String> online = new ArrayList<String>();
		for (IPlayer player : server.getOnlinePlayers())
		{
			if (executor instanceof IPlayer && ((IPlayer) executor).shouldNotSee(player))
				continue;
			online.add(player.getPrettyName());
		}

		return String.format(
			"There are %d/%d players connected:\n %s",
			online.size(),
			server.getMaxPlayers(),
			StringUtils.join(online, ", ")
		);
	}

	private final IServer server;
}
