package no.runsafe.UserControl.command;

import no.runsafe.framework.api.command.ExecutableCommand;
import no.runsafe.framework.api.command.ICommandExecutor;
import no.runsafe.framework.api.command.argument.PlayerArgument;
import no.runsafe.framework.api.command.argument.RequiredArgument;
import no.runsafe.framework.api.player.IPlayer;
import no.runsafe.framework.internal.wrapper.ObjectUnwrapper;
import no.runsafe.framework.minecraft.RunsafeServer;
import no.runsafe.framework.minecraft.player.RunsafeAmbiguousPlayer;
import org.bukkit.entity.Player;

import java.util.Map;

public class BanIP extends ExecutableCommand
{
	public BanIP()
	{
		super(
			"banip", "Bans an IP from connecting to this server", "runsafe.usercontrol.ban.ip",
			new PlayerArgument(), new RequiredArgument("reason")
		);
	}

	@Override
	public String OnExecute(ICommandExecutor executor, Map<String, String> parameters)
	{
		IPlayer victim = RunsafeServer.Instance.getPlayer(parameters.get("player"));
		if (victim == null)
			return "Player not found";

		if (victim instanceof RunsafeAmbiguousPlayer)
			return victim.toString();

		if (victim.hasPermission("runsafe.usercontrol.ban.immune"))
			return "You cannot ban that player";

		String ip = ((Player)ObjectUnwrapper.convert(victim)).getAddress().getAddress().getHostAddress();
		RunsafeServer.Instance.banIP(ip);
		return String.format("Banned IP %s from the server", ip);
	}
}
