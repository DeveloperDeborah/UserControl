package no.runsafe.UserControl.command;

import no.runsafe.framework.command.RunsafeCommand;
import no.runsafe.framework.server.RunsafeServer;
import no.runsafe.framework.server.player.RunsafePlayer;

public class UnBanIP extends RunsafeCommand
{
	public UnBanIP()
	{
		super("unbanip", "ip-address");
	}

	@Override
	public String requiredPermission()
	{
		return "runsafe.unban.ip";
	}

	@Override
	public String OnExecute(RunsafePlayer executor, String[] args)
	{
		String ip = getArg("ip-address");
		RunsafeServer.Instance.unbanIp(ip);
		// TODO Log unbanning reason
		//String reason = StringUtils.join(args, " ", 1, args.length);
		return String.format("Unbanned IP %s from the server", ip);
	}
}
