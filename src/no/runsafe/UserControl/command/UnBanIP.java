package no.runsafe.UserControl.command;

import no.runsafe.UserControl.database.IPBanList;
import no.runsafe.framework.command.RunsafeCommand;
import no.runsafe.framework.server.RunsafeServer;
import no.runsafe.framework.server.player.RunsafePlayer;
import org.apache.commons.lang.StringUtils;

public class UnBanIP extends RunsafeCommand
{
	public UnBanIP(IPBanList banList)
	{
		super("unbanip", "ip-address");
		ipBanList = banList;
	}

	@Override
	public String requiredPermission()
	{
		return "runsafe.unban.ip";
	}

	@Override
	public String OnExecute(RunsafePlayer executor, String[] args)
	{
		// TODO Log unbanning reason
		String reason = StringUtils.join(args, " ", 1, args.length);
		RunsafePlayer victim = RunsafeServer.Instance.getPlayer(getArg("player"));
		String ip = victim.getRawPlayer().getAddress().getAddress().getHostAddress();
		ipBanList.unBanIp(ip);
		return String.format("Unbanned IP %s from the server", ip);
	}

	IPBanList ipBanList;
}
