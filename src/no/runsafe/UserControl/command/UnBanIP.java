package no.runsafe.UserControl.command;

import no.runsafe.framework.api.command.ExecutableCommand;
import no.runsafe.framework.api.command.ICommandExecutor;
import no.runsafe.framework.minecraft.RunsafeServer;

import java.util.Map;

public class UnBanIP extends ExecutableCommand
{
	public UnBanIP()
	{
		super("unbanip", "Bans an IP from connecting to this server", "runsafe.usercontrol.unban.ip", "ip-address");
	}

	@Override
	public String OnExecute(ICommandExecutor executor, Map<String, String> parameters)
	{
		String ip = parameters.get("ip-address");
		RunsafeServer.Instance.unbanIP(ip);
		return String.format("Unbanned IP %s from the server", ip);
	}
}
