package no.runsafe.UserControl.command;

import no.runsafe.framework.api.command.ICommandExecutor;
import no.runsafe.framework.internal.command.ExecutableCommand;
import no.runsafe.framework.minecraft.RunsafeServer;

import java.util.HashMap;

public class UnBanIP extends ExecutableCommand
{
	public UnBanIP()
	{
		super("unbanip", "Bans an IP from connecting to this server", "runsafe.usercontrol.unban.ip", "ip-address");
	}

	@Override
	public String OnExecute(ICommandExecutor executor, HashMap<String, String> parameters)
	{
		String ip = parameters.get("ip-address");
		RunsafeServer.Instance.unbanIp(ip);
		return String.format("Unbanned IP %s from the server", ip);
	}
}
