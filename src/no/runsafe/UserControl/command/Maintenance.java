package no.runsafe.UserControl.command;

import no.runsafe.UserControl.MaintenanceHandler;
import no.runsafe.framework.api.command.ExecutableCommand;
import no.runsafe.framework.api.command.ICommandExecutor;
import no.runsafe.framework.api.command.argument.IArgumentList;

public class Maintenance extends ExecutableCommand
{
	public Maintenance(MaintenanceHandler handler)
	{
		super("maintenance", "Sets the servers current maintenance state", "runsafe.usercontrol.maintenance.set");
		this.handler = handler;
	}

	@Override
	public String OnExecute(ICommandExecutor executor, IArgumentList parameters)
	{
		this.handler.setMaintenance(!this.handler.isMaintenance());

		if (this.handler.isMaintenance())
			return "&eThe server is now closed for maintenance.";

		return "&eThe server is now open again.";
	}

	private final MaintenanceHandler handler;
}
