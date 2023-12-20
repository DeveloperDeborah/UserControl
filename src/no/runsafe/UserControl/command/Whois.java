package no.runsafe.UserControl.command;

import no.runsafe.framework.api.IConfiguration;
import no.runsafe.framework.api.IScheduler;
import no.runsafe.framework.api.command.AsyncCommand;
import no.runsafe.framework.api.command.ICommandExecutor;
import no.runsafe.framework.api.command.argument.IArgumentList;
import no.runsafe.framework.api.command.argument.Player;
import no.runsafe.framework.api.event.plugin.IConfigurationChanged;
import no.runsafe.framework.api.player.IPlayer;

import java.util.Map;

public class Whois extends AsyncCommand implements IConfigurationChanged
{
	public Whois(IScheduler scheduler)
	{
		super(
			"whois", "Queries the server about a player, printing available information.", "runsafe.usercontrol.whois",
			scheduler,
			new Player().require()
		);
	}

	@Override
	public String OnAsyncExecute(ICommandExecutor executor, IArgumentList parameters)
	{
		IPlayer target = parameters.getValue("player");
		if (target == null)
			return null;
		Map<String, String> data = target.getData();
		if (data == null || data.isEmpty())
			return String.format("No data found for player %s.", target.getPrettyName());
		StringBuilder info = new StringBuilder();
		info.append(String.format("Whois results for %s:", target.getPrettyName()));
		boolean showAll = executor == null || executor.hasPermission("runsafe.usercontrol.whois.see.*");
		for (String key : data.keySet())
		{
			if (
				!(showAll || executor.hasPermission(
					String.format("runsafe.usercontrol.whois.see.%s", key.toLowerCase().replace(' ', '.'))
				)))
				continue;
			String label = getLabel(key);
			String value = formatOutput(key, data.get(key));
			String format = "\n- &6%s: &r%s";
			if (value == null)
				format = "\n- &6%s: &4NULL&r";
			else if (value.equals("true"))
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
		labels = configuration.getConfigValuesAsMap("whois.labels");
		outFormat = configuration.getConfigValuesAsMap("whois.formats");
	}

	private String getLabel(String key)
	{
		if (labels == null)
			return key;
		String label = labels.get(key.toLowerCase().replace(' ', '.'));
		return label == null || label.isEmpty() ? key : label;
	}

	private String formatOutput(String key, String value)
	{
		if (outFormat == null)
			return value;
		String format = outFormat.get(key.toLowerCase().replace(' ', '.'));
		if (format == null || format.isEmpty())
			return value;
		return String.format(format, value);
	}

	private Map<String, String> labels;
	private Map<String, String> outFormat;
}
