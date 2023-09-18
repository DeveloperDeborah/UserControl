package no.runsafe.UserControl.command;

import no.runsafe.UserControl.BanEnforcer;
import no.runsafe.UserControl.database.PlayerDatabase;
import no.runsafe.framework.api.command.ExecutableCommand;
import no.runsafe.framework.api.command.ICommandExecutor;
import no.runsafe.framework.api.command.argument.IArgumentList;
import no.runsafe.framework.api.command.argument.Player;
import no.runsafe.framework.api.command.argument.RequiredArgument;
import no.runsafe.framework.api.player.IPlayer;
import no.runsafe.framework.api.server.IPlayerManager;

public class UnBan extends ExecutableCommand
{
	public UnBan(PlayerDatabase playerDatabase, BanEnforcer enforcer, IPlayerManager playerManager)
	{
		super(
			"unban", "Unbans a player from the server", "runsafe.usercontrol.unban",
			new Player().require(), new RequiredArgument("reason")
		);
		playerdb = playerDatabase;
		this.enforcer = enforcer;
		this.playerManager = playerManager;
	}

	@Override
	public String OnExecute(ICommandExecutor executor, IArgumentList parameters)
	{
		enforcer.flushCache();
		IPlayer player = parameters.getValue("player");
		if (player == null)
			return null;

		if (playerdb.getData(player).getBanned() == null)
			return String.format("Player %s is not banned.", player.getPrettyName());

		// TODO Log unbanning reason
		playerdb.logPlayerUnban(player);
		playerManager.unbanPlayer(player);
		return String.format("Player %s was unbanned.", player.getPrettyName());
	}

	private final PlayerDatabase playerdb;
	private final BanEnforcer enforcer;
	private final IPlayerManager playerManager;
}
