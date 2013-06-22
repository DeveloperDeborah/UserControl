package no.runsafe.UserControl.command;

import no.runsafe.framework.api.IConfiguration;
import no.runsafe.framework.api.command.ExecutableCommand;
import no.runsafe.framework.api.command.ICommandExecutor;
import no.runsafe.framework.api.event.plugin.IConfigurationChanged;
import no.runsafe.framework.api.hook.IPlayerPermissions;
import no.runsafe.framework.minecraft.RunsafeServer;
import no.runsafe.framework.minecraft.player.RunsafeAmbiguousPlayer;
import no.runsafe.framework.minecraft.player.RunsafePlayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Rank extends ExecutableCommand implements IConfigurationChanged
{
	public Rank(IPlayerPermissions permissions)
	{
		super("rank", "Sets a players rank", "runsafe.usercontrol.rank.<rank>", "player", "rank");
		this.permissions = permissions;
	}

	@Override
	public List<String> getParameterOptions(String parameter)
	{
		return parameter.equals("rank") ? groups : null;
	}

	@Override
	protected String OnExecute(ICommandExecutor executor, HashMap<String, String> parameters)
	{
		RunsafePlayer player = RunsafeServer.Instance.getPlayer(parameters.get("player"));

		if (player == null)
			return String.format("Unable to locate a player named %s", parameters.get("player"));

		if (player instanceof RunsafeAmbiguousPlayer)
			return player.toString();

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

		this.permissions.setGroup(player, rank);
		if (this.messages.containsKey(rank) && player.isOnline())
			player.sendColouredMessage(this.messages.get(rank));

		return String.format("&2%s set to %s.", player.getName(), rank);
	}

	@Override
	public void OnConfigurationChanged(IConfiguration configuration)
	{
		this.groups = new ArrayList<String>();
		for (String group : this.permissions.getGroups())
			groups.add(group.toLowerCase());

		this.messages = configuration.getConfigValuesAsMap("rankMessages");
	}

	private boolean isInGroup(RunsafePlayer player, String group)
	{
		for (String playerGroup : player.getGroups())
			if (playerGroup.equalsIgnoreCase(group))
				return true;

		return false;
	}

	private List<String> groups;
	private Map<String, String> messages;
	private IPlayerPermissions permissions;
}