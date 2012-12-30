package no.runsafe.UserControl.command;

import no.runsafe.framework.command.RunsafeCommand;
import no.runsafe.framework.server.RunsafeServer;
import no.runsafe.framework.server.player.RunsafePlayer;
import org.apache.commons.lang.StringUtils;

public class SuDo extends RunsafeCommand
{
	public SuDo()
	{
		super("sudo", "player", "command");
	}

	@Override
	public String requiredPermission()
	{
		return "runsafe.usercontrol.sudo";
	}

	@Override
	public String OnExecute(RunsafePlayer executor, String[] args)
	{
		RunsafePlayer target = RunsafeServer.Instance.getPlayer(getArg("player"));
		if(target.hasPermission("runsafe.usercontrol.sudo.immune"))
			return "You cannot make that user run commands";

		String command = StringUtils.join(args, " ", 1, args.length);
		target.getRawPlayer().performCommand(command);
		return String.format("Forced %s to run /%s", target.getName(), command);
	}
}
