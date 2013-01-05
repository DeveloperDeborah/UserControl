package no.runsafe.UserControl.command;

import no.runsafe.framework.command.RunsafeCommand;
import no.runsafe.framework.server.RunsafeServer;
import no.runsafe.framework.server.player.RunsafeAmbiguousPlayer;
import no.runsafe.framework.server.player.RunsafePlayer;
import org.apache.commons.lang.StringUtils;

public class BanIP extends RunsafeCommand
{
	public BanIP()
	{
		super("banip", "player", "reason");
	}

	@Override
	public String requiredPermission()
	{
		return "runsafe.usercontrol.ban.ip";
	}

	@Override
	public String OnExecute(RunsafePlayer executor, String[] args)
	{
		//String reason = StringUtils.join(args, " ", 1, args.length);
		RunsafePlayer victim = RunsafeServer.Instance.getPlayer(getArg("player"));
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
