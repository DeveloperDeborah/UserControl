package no.runsafe.UserControl.command;

import no.runsafe.framework.command.AsyncCommand;
import no.runsafe.framework.configuration.IConfiguration;
import no.runsafe.framework.event.IConfigurationChanged;
import no.runsafe.framework.server.ICommandExecutor;
import no.runsafe.framework.server.RunsafeServer;
import no.runsafe.framework.server.player.RunsafePlayer;
import no.runsafe.framework.timer.IScheduler;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;

public class Whois extends AsyncCommand implements IConfigurationChanged
{
	public Whois(IScheduler scheduler)
	{
		super("whois", "Queries the server about a player, printing available information.", "runsafe.usercontrol.whois", scheduler, "player");
	}

	@Override
	public String OnAsyncExecute(ICommandExecutor executor, HashMap<String, String> parameters, String[] arguments)
	{
		RunsafePlayer target = RunsafeServer.Instance.getPlayer(parameters.get("player"));
		if (target == null)
			return String.format("Could not locate a player using %s", parameters.get("player"));
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
			String format = "\n- &6%s: &r%s";
			if (value.equals("true"))
				format = "\n- &6%s: &a%s&r";
			else if (value.equals("false"))
				format = "\n- &6%s: &c%s&r";
			info.append(String.format(format, label, value));
		}
		return info.toString();
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
