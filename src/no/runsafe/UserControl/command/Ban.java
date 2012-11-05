package no.runsafe.UserControl.command;

import no.runsafe.framework.command.ICommand;
import no.runsafe.framework.command.RunsafeCommand;
import no.runsafe.framework.configuration.IConfiguration;
import no.runsafe.framework.event.IConfigurationChanged;
import no.runsafe.framework.server.RunsafeServer;
import no.runsafe.framework.server.player.RunsafePlayer;
import org.apache.commons.lang.StringUtils;

import java.util.Collection;

public class Ban extends RunsafeCommand implements IConfigurationChanged
{
	public Ban()
	{
		super("ban", "player", "reason");
	}

	@Override
	public String requiredPermission()
	{
		return "runsafe.usercontrol.ban";
	}

	@Override
	public String OnExecute(RunsafePlayer executor, String[] args)
	{
		String source = executor == null ? "console" : executor.getName();
		String reason = StringUtils.join(args, " ", 1, args.length);
		// TODO Store ban reason
		RunsafePlayer victim = RunsafeServer.Instance.getPlayer(getArg("player"));
		if (victim == null || !victim.isOnline())
		{
			victim.setBanned(true);
			return String.format("Banned offline player %s.", getArg("player"));
		}
		victim.kick(String.format(banMessageFormat, source, reason));
		victim.setBanned(true);
		return null;
	}

	@Override
	public void OnConfigurationChanged(IConfiguration configuration)
	{
		banMessageFormat = configuration.getConfigValueAsString("messages.ban");
	}

	String banMessageFormat = "%2$s";
}
