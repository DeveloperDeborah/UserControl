package no.runsafe.UserControl.command;

import no.runsafe.framework.api.command.ExecutableCommand;
import no.runsafe.framework.api.command.ICommandExecutor;
import no.runsafe.framework.minecraft.RunsafeServer;
import no.runsafe.framework.minecraft.player.RunsafeAmbiguousPlayer;
import no.runsafe.framework.minecraft.player.RunsafePlayer;

import java.util.Map;

public class BanIP extends ExecutableCommand
{
	public BanIP()
	{
		super("banip", "Bans an IP from connecting to this server", "runsafe.usercontrol.ban.ip", "player", "reason");
	}

	@Override
	public String OnExecute(ICommandExecutor executor, Map<String, String> parameters)
	{
		RunsafePlayer victim = RunsafeServer.Instance.getPlayer(parameters.get("player"));
		if (victim == null)
			return "Player not found";

		if (victim instanceof RunsafeAmbiguousPlayer)
			return victim.toString();

		if (victim.hasPermission("runsafe.usercontrol.ban.immune"))
			return "You cannot ban that player";

		String ip = victim.getRawPlayer().getAddress().getAddress().getHostAddress();
		RunsafeServer.Instance.banIP(ip);
		return String.format("Banned IP %s from the server", ip);
	}
}
