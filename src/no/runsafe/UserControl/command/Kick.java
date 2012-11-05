package no.runsafe.UserControl.command;

import no.runsafe.framework.command.RunsafeCommand;
import no.runsafe.framework.configuration.IConfiguration;
import no.runsafe.framework.event.IConfigurationChanged;
import no.runsafe.framework.server.RunsafeServer;
import no.runsafe.framework.server.player.RunsafePlayer;
import org.apache.commons.lang.StringUtils;

public class Kick extends RunsafeCommand implements IConfigurationChanged
{
	public Kick()
	{
		super("kick", "player", "reason");
	}

	@Override
	public String requiredPermission()
	{
		return "runsafe.usercontrol.kick";
	}

	@Override
	public String OnExecute(RunsafePlayer executor, String[] args)
	{
		RunsafePlayer victim = RunsafeServer.Instance.getPlayer(getArg("player"));
		if (victim == null || !victim.isOnline())
			return String.format("Player %s not found or offline!", getArg("player"));

		String source = executor == null ? "console" : executor.getName();
		String reason = StringUtils.join(args, " ", 1, args.length);
		victim.kick(String.format(kickMessageFormat, source, reason));
		return null;
	}

	@Override
	public void OnConfigurationChanged(IConfiguration configuration)
	{
		kickMessageFormat = configuration.getConfigValueAsString("messages.kick");
	}

	String kickMessageFormat = "%2$s";
}
