package no.runsafe.UserControl.command;

import no.runsafe.framework.command.RunsafeAsyncCommand;
import no.runsafe.framework.configuration.IConfiguration;
import no.runsafe.framework.event.IConfigurationChanged;
import no.runsafe.framework.server.RunsafeServer;
import no.runsafe.framework.server.player.RunsafePlayer;
import no.runsafe.framework.timer.IScheduler;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;

public class Whois extends RunsafeAsyncCommand implements IConfigurationChanged
{
	public Whois(IScheduler scheduler)
	{
		super("whois", scheduler, "player");
	}

	@Override
	public String OnExecute(RunsafePlayer executor, String[] args)
	{
		RunsafePlayer target = RunsafeServer.Instance.getPlayer(getArg("player"));
		HashMap<String, String> data = target.getData();
		if (data == null || data.size() == 0)
			return String.format("No data found for player %s.", target.getPrettyName());
		StringBuilder info = new StringBuilder();
		info.append(String.format("Whois results for %s:", target.getPrettyName()));
		boolean showAll = executor == null || executor.hasPermission("runsafe.usercontrol.whois.see.*");
		for (String key : data.keySet())
		{
			if (
				!(showAll || executor.hasPermission(
					String.format("runsafe.usercontrol.see.%s", key.toLowerCase().replace(' ', '.'))
				)))
				continue;
			String label = getLabel(key);
			String value = formatOutput(key, data.get(key));
			ChatColor color = ChatColor.RESET;
			if (value.equals("true"))
				color = ChatColor.GREEN;
			else if (value.equals("false"))
				color = ChatColor.RED;
			info.append(String.format("\n- %s%s: %s%s%s", ChatColor.GOLD, label, color, value, ChatColor.RESET));
		}
		return info.toString();
	}

	@Override
	public String requiredPermission()
	{
		return "runsafe.usercontrol.whois";
	}

	@Override
	public String getDescription()
	{
		return "Queries the server about a player, printing available information.";
	}

	@Override
	public void OnConfigurationChanged(IConfiguration configuration)
	{
		labels = configuration.getSection("whois.labels");
		outFormat = configuration.getSection("whois.formats");
	}

	private String getLabel(String key)
	{
		if (labels == null)
			return key;
		String label = labels.getString(key.toLowerCase().replace(' ', '.'));
		return label == null || label.isEmpty() ? key : label;
	}

	private String formatOutput(String key, String value)
	{
		if (outFormat == null)
			return value;
		String format = outFormat.getString(key.toLowerCase().replace(' ', '.'));
		if (format == null || format.isEmpty())
			return value;
		return String.format(format, value);
	}

	private ConfigurationSection labels;
	private ConfigurationSection outFormat;
}
