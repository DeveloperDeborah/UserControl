package no.runsafe.UserControl.command;

import no.runsafe.framework.api.IConfiguration;
import no.runsafe.framework.api.IServer;
import no.runsafe.framework.api.command.ExecutableCommand;
import no.runsafe.framework.api.command.ICommandExecutor;
import no.runsafe.framework.api.command.argument.IArgumentList;
import no.runsafe.framework.api.command.argument.OnlinePlayerRequired;
import no.runsafe.framework.api.command.argument.RequiredArgument;
import no.runsafe.framework.api.command.argument.UserGroupArgument;
import no.runsafe.framework.api.event.IServerReady;
import no.runsafe.framework.api.event.plugin.IConfigurationChanged;
import no.runsafe.framework.api.hook.IPlayerExtensions;
import no.runsafe.framework.api.player.IAmbiguousPlayer;
import no.runsafe.framework.api.player.IPlayer;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Rank extends ExecutableCommand implements IConfigurationChanged, IServerReady
{
	public Rank(IServer server, IPlayerExtensions extensions)
	{
		super(
			"rank", "Sets a players rank", "runsafe.usercontrol.rank.<rank>",
			new OnlinePlayerRequired(), new UserGroupArgument("rank", true)
		);
		this.server = server;
		this.extensions = extensions;
	}

	@Override
	public String OnExecute(ICommandExecutor executor, IArgumentList parameters)
	{
		IPlayer player = server.getPlayer(parameters.get("player"));

		if (player == null)
			return String.format("Unable to locate a player named %s", parameters.get("player"));

		if (player.getName().equals(executor.getName()))
			return "&cYou may not change your own rank.";

		for (String group : player.getGroups())
		{
			String permission = String.format("runsafe.usercontrol.rank.%s", group);
			if (!executor.hasPermission(permission))
				return String.format("&cYou may not change the group of %s", player.getPrettyName());
		}

		String rank = parameters.get("rank").toLowerCase();
		if (!this.groups.contains(rank))
			return "&cThat rank does not exist.";

		if (isInGroup(player, rank))
			return "&cThat player is already that rank.";

		if (!player.setGroup(rank))
			return "Group set failed, somehow..";
		if (this.messages.containsKey(rank) && player.isOnline())
			player.sendColouredMessage(this.messages.get(rank));

		return String.format("&2%s set to %s.", player.getPrettyName(), rank);
	}

	@Override
	public void OnConfigurationChanged(IConfiguration configuration)
	{
		OnServerReady();
		this.messages = configuration.getConfigValuesAsMap("rankMessages");
	}

	@Override
	public void OnServerReady()
	{
		this.groups.clear();
		for (String group : extensions.getGroups())
			groups.add(group.toLowerCase());
	}

	private boolean isInGroup(IPlayer player, String group)
	{
		for (String playerGroup : player.getGroups())
			if (playerGroup.equalsIgnoreCase(group))
				return true;

		return false;
	}

	private List<String> groups = new ArrayList<String>();
	private Map<String, String> messages;
	private final IServer server;
	private final IPlayerExtensions extensions;
}
