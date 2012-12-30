package no.runsafe.UserControl.command;

import no.runsafe.UserControl.database.IPBanList;
import no.runsafe.framework.command.RunsafeCommand;
import no.runsafe.framework.configuration.IConfiguration;
import no.runsafe.framework.event.IConfigurationChanged;
import no.runsafe.framework.server.RunsafeServer;
import no.runsafe.framework.server.player.RunsafePlayer;
import org.apache.commons.lang.StringUtils;

public class BanIP extends RunsafeCommand implements IConfigurationChanged
{
	public BanIP(IPBanList banList)
	{
		super("banip", "player", "reason");
		ipBanList = banList;
	}

	@Override
	public String requiredPermission()
	{
		return "runsafe.ban.ip";
	}

	@Override
	public String OnExecute(RunsafePlayer executor, String[] args)
	{
		String reason = StringUtils.join(args, " ", 1, args.length);
		RunsafePlayer victim = RunsafeServer.Instance.getPlayer(getArg("player"));
		if(victim == null)
			return "Player not found";

		if(victim.hasPermission("runsafe.usercontrol.ban.immune"))
			return "You cannot ban that player";
		
		String ip = victim.getRawPlayer().getAddress().getAddress().getHostAddress();
		ipBanList.banIp(ip, executor, reason);
		return String.format("Banned IP %s from the server", ip);
	}

	@Override
	public void OnConfigurationChanged(IConfiguration configuration)
	{
		banMessage = configuration.getConfigValueAsString("messages.ban");
	}

	String banMessage = "%2$s";
	IPBanList ipBanList;
}
