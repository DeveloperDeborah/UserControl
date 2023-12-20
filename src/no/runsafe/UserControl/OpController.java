package no.runsafe.UserControl;

import no.runsafe.framework.api.IConfiguration;
import no.runsafe.framework.api.IScheduler;
import no.runsafe.framework.api.event.player.IPlayerLoginEvent;
import no.runsafe.framework.api.event.player.IPlayerOperatorEvent;
import no.runsafe.framework.api.event.plugin.IConfigurationChanged;
import no.runsafe.framework.api.player.IPlayer;
import no.runsafe.framework.minecraft.event.player.RunsafeOperatorEvent;
import no.runsafe.framework.minecraft.event.player.RunsafePlayerLoginEvent;
import no.runsafe.framework.timer.Timer;

import java.time.Duration;
import java.time.Instant;
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
		for (IPlayer player : opExpiration.keySet())
			if (opExpiration.get(player).isBefore(Instant.now()))
			{
				player.deOP();
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
		timerDeOp = duration > 0 ? Duration.ofSeconds(duration) : null;
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
			operatorEvent.getPlayer().sendMessage("You are now an operator.");
			if (timerDeOp != null)
			{
				opExpiration.put(operatorEvent.getPlayer(), Instant.now().plus(timerDeOp));
				start();
			}
		}
		else if (timerDeOp != null)
		{
			operatorEvent.getPlayer().sendMessage("You are no longer an operator.");
			opExpiration.remove(operatorEvent.getPlayer());
		}
	}

	private boolean loginDeOp;
	private Duration timerDeOp;
	private final ConcurrentHashMap<IPlayer, Instant> opExpiration = new ConcurrentHashMap<>();
}
