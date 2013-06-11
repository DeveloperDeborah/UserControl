package no.runsafe.UserControl;

import no.runsafe.framework.minecraft.RunsafeLocation;

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

	public void removeRedirectLocation()
	{
		this.redirectLocation = null;
	}

	private RunsafeLocation redirectLocation;
}
