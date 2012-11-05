package no.runsafe.UserControl;

import no.runsafe.UserControl.command.*;
import no.runsafe.framework.RunsafePlugin;

public class Plugin extends RunsafePlugin
{
	@Override
	protected void PluginSetup()
	{
		addComponent(Ban.class);
		addComponent(BanIP.class);
		addComponent(Kick.class);
		addComponent(KickAll.class);
		addComponent(SuDo.class);
		addComponent(TempBan.class);
		addComponent(UnBan.class);
		addComponent(UnBanIP.class);
	}
}
