package no.runsafe.UserControl.command;

import no.runsafe.UserControl.MaintenanceHandler;
import no.runsafe.framework.api.command.ExecutableCommand;
import no.runsafe.framework.api.command.ICommandExecutor;

import java.util.HashMap;

public class Maintenance extends ExecutableCommand
{
	public Maintenance(MaintenanceHandler handler)
	{
		super("maintenance", "Sets the servers current maintenance state", "runsafe.usercontrol.maintenance.set");
		this.handler = handler;
	}

	@Override
	protected String OnExecute(ICommandExecutor executor, HashMap<String, String> parameters)
	{
		this.handler.setMaintenance(!this.handler.isMaintenance());

		if (this.handler.isMaintenance())
			return "&eThe server is now closed for maintenance.";

		return "&eThe server is now open again.";
	}

	private MaintenanceHandler handler;
}
