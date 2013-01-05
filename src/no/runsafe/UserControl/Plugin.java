package no.runsafe.UserControl;

import no.runsafe.UserControl.command.*;
import no.runsafe.UserControl.database.PlayerDatabase;
import no.runsafe.UserControl.database.PlayerKickLog;
import no.runsafe.UserControl.database.PlayerSessionLog;
import no.runsafe.framework.RunsafeConfigurablePlugin;
import no.runsafe.framework.command.ICommand;
import no.runsafe.framework.command.RunsafeCommand;

public class Plugin extends RunsafeConfigurablePlugin
{
	@Override
	protected void PluginSetup()
	{
		addComponent(PlayerDatabase.class);
		addComponent(PlayerSessionLog.class);
		addComponent(PlayerKickLog.class);
		addComponent(SessionLogger.class);
		addComponent(Ban.class);
		addComponent(BanIP.class);
		addComponent(Kick.class);
		addComponent(KickAll.class);
		addComponent(SuDo.class);
		addComponent(TempBan.class);
		addComponent(UnBan.class);
		addComponent(UnBanIP.class);
		addComponent(BanEnforcer.class);
		addComponent(Seen.class);

		ICommand usercontrol = new RunsafeCommand("usercontrol");
		usercontrol.addSubCommand(getInstance(EssentialsImport.class));
		addComponent(usercontrol);
	}
}
