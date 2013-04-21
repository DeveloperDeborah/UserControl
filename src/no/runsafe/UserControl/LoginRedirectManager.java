package no.runsafe.UserControl;

import no.runsafe.framework.server.RunsafeLocation;

public class LoginRedirectManager
{
	public void setRedirectLocation(RunsafeLocation location)
	{
		this.redirectLocation = location;
	}

	public RunsafeLocation getRedirectLocation()
	{
		return this.redirectLocation;
	}

	public boolean hasRedirectLocation()
	{
		return (this.redirectLocation != null);
	}

	private RunsafeLocation redirectLocation;
}
