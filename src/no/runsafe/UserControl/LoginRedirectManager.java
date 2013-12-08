package no.runsafe.UserControl;

import no.runsafe.framework.api.ILocation;

public class LoginRedirectManager
{
	public void setRedirectLocation(ILocation location)
	{
		this.redirectLocation = location;
	}

	public ILocation getRedirectLocation()
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

	private ILocation redirectLocation;
}
