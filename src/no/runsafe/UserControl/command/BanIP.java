package no.runsafe.UserControl.command;

import no.runsafe.framework.command.ExecutableCommand;
import no.runsafe.framework.server.ICommandExecutor;
import no.runsafe.framework.server.RunsafeServer;
import no.runsafe.framework.server.player.RunsafeAmbiguousPlayer;
import no.runsafe.framework.server.player.RunsafePlayer;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;

public class BanIP extends ExecutableCommand
{
	public BanIP()
	{
		super("banip", "Bans an IP from connecting to this server", "runsafe.usercontrol.ban.ip", "player", "reason");
	}

	@Override
	public String OnExecute(ICommandExecutor executor, HashMap<String, String> parameters)
	{
		RunsafePlayer victim = RunsafeServer.Instance.getPlayer(parameters.get("player"));
		if (victim == null)
			return "Player not found";

		if (victim instanceof RunsafeAmbiguousPlayer)
		{
			return String.format(
				"Multiple matches found: %s",
				StringUtils.join(((RunsafeAmbiguousPlayer) victim).getAmbiguity(), ", ")
			);
		}

		if (victim.hasPermission("runsafe.usercontrol.ban.immune"))
			return "You cannot ban that player";

		String ip = victim.getRawPlayer().getAddress().getAddress().getHostAddress();
		RunsafeServer.Instance.banIP(ip);
		return String.format("Banned IP %s from the server", ip);
	}
}
