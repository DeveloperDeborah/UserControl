package no.runsafe.UserControl.command;

import no.runsafe.UserControl.BanEnforcer;
import no.runsafe.UserControl.database.PlayerDatabase;
import no.runsafe.framework.api.command.ExecutableCommand;
import no.runsafe.framework.api.command.ICommandExecutor;
import no.runsafe.framework.minecraft.RunsafeServer;
import no.runsafe.framework.minecraft.player.RunsafeAmbiguousPlayer;
import no.runsafe.framework.minecraft.player.RunsafePlayer;

import java.util.HashMap;

public class UnBan extends ExecutableCommand
{
	public UnBan(PlayerDatabase playerDatabase, BanEnforcer enforcer)
	{
		super("unban", "Unbans a player from the server", "runsafe.usercontrol.unban", "player", "reason");
		playerdb = playerDatabase;
		this.enforcer = enforcer;
	}

	@Override
	public String OnExecute(ICommandExecutor executor, HashMap<String, String> parameters)
	{
		enforcer.flushCache();
		RunsafePlayer player = RunsafeServer.Instance.getPlayer(parameters.get("player"));
		if (player instanceof RunsafeAmbiguousPlayer)
			return player.toString();
		if (!player.isBanned())
			return String.format("Player %s is not banned.", player.getPrettyName());

		// TODO Log unbanning reason
		playerdb.logPlayerUnban(player);
		player.setBanned(false);
		return String.format("Player %s was unbanned.", player.getPrettyName());
	}

	private final PlayerDatabase playerdb;
	private final BanEnforcer enforcer;
}
