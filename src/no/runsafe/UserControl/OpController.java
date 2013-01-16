package no.runsafe.UserControl;

import no.runsafe.framework.configuration.IConfiguration;
import no.runsafe.framework.event.IConfigurationChanged;
import no.runsafe.framework.event.player.IPlayerLoginEvent;
import no.runsafe.framework.event.player.IPlayerOperatorEvent;
import no.runsafe.framework.server.RunsafeServer;
import no.runsafe.framework.server.event.player.RunsafeOperatorEvent;
import no.runsafe.framework.server.event.player.RunsafePlayerLoginEvent;
import no.runsafe.framework.server.player.RunsafePlayer;
import no.runsafe.framework.timer.IScheduler;
import no.runsafe.framework.timer.Timer;
import org.joda.time.DateTime;
import org.joda.time.Duration;

import java.util.concurrent.ConcurrentHashMap;

public class OpController extends Timer implements IConfigurationChanged, IPlayerLoginEvent, IPlayerOperatorEvent
{
	public OpController(IScheduler scheduler)
	{
		super(scheduler, false);
		delay = 100;
		period = 100;
	}

	@Override
	public void OnElapsed()
	{
		for (String player : opExpiration.keySet())
			if (opExpiration.get(player).isBefore(DateTime.now()))
			{
				RunsafePlayer operator = RunsafeServer.Instance.getPlayerExact(player);
				operator.deOP();
				opExpiration.remove(player);
			}

		if (opExpiration.isEmpty())
			stop();
	}

	@Override
	public void OnConfigurationChanged(IConfiguration configuration)
	{
		loginDeOp = configuration.getConfigValueAsBoolean("control.op.login");
		int duration = configuration.getConfigValueAsInt("control.op.timer");
		timerDeOp = duration > 0 ? Duration.standardSeconds(duration) : null;
	}

	@Override
	public void OnPlayerLogin(RunsafePlayerLoginEvent event)
	{
		if (loginDeOp && event.getPlayer().isOP())
			event.getPlayer().deOP();
	}

	@Override
	public void OnPlayerOP(RunsafeOperatorEvent operatorEvent)
	{
		if (operatorEvent.wasMadeOP())
		{
			operatorEvent.getPlayer().sendMessage(String.format("You are now an operator."));
			if (timerDeOp != null)
			{
				opExpiration.put(operatorEvent.getPlayer().getName(), DateTime.now().plus(timerDeOp));
				start();
			}
		}
		else if (timerDeOp != null)
		{
			operatorEvent.getPlayer().sendMessage(String.format("You are no longer an operator."));
			opExpiration.remove(operatorEvent.getPlayer().getName());
		}
	}

	private boolean loginDeOp;
	private Duration timerDeOp;
	private final ConcurrentHashMap<String, DateTime> opExpiration = new ConcurrentHashMap<String, DateTime>();
}
