package no.runsafe.UserControl.command;

import no.runsafe.framework.command.RunsafeCommand;
import no.runsafe.framework.configuration.IConfiguration;
import no.runsafe.framework.event.IConfigurationChanged;
import no.runsafe.framework.server.RunsafeServer;
import no.runsafe.framework.server.player.RunsafePlayer;
import org.apache.commons.lang.StringUtils;

public class KickAll extends RunsafeCommand implements IConfigurationChanged
{
	public KickAll()
	{
		super("kickall", "reason");
	}

	@Override
	public String requiredPermission()
	{
		return "runsafe.usercontrol.kickall";
	}

	@Override
	public String OnExecute(RunsafePlayer executor, String[] args)
	{
		String source = executor == null ? "console" : executor.getName();
		String reason = StringUtils.join(args, " ");
		int n = 0;

		for (RunsafePlayer victim : RunsafeServer.Instance.getOnlinePlayers())
			if (executor == null || !victim.getName().equals(executor.getName()))
			{
				victim.kick(String.format(kickMessageFormat, source, reason));
				n++;
			}
		return String.format("Kicked %d players from the server.", n);
	}

	@Override
	public void OnConfigurationChanged(IConfiguration configuration)
	{
		kickMessageFormat = configuration.getConfigValueAsString("messages.kick");
	}

	String kickMessageFormat = "%2$s";
}
