package no.runsafe.UserControl;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

public class TimeFormatter
{
	public static String formatDuration(Duration duration)
	{
		if (duration == null)
			return "Invalid Time";

		return formatMillis(duration.toMillis());
	}

	public static String formatInstant(Instant instant)
	{
		if (instant == null)
			return "Invalid Time";

		return formatMillis(Duration.between(instant, Instant.now()).toMillis());
	}

	public static String formatDate(Instant date)
	{
		if (date == null)
			return "Invalid Date";

		return date.atZone(ZoneId.systemDefault()).toString().replace("T", " ").substring(0,16);
	}

	private static String formatMillis(long millis)
	{
		if (millis < 1000L)
			return "Less than one second";
		long millisOriginalValue = millis;
		int years = 0;
		int months = 0;
		int weeks = 0;
		int days = 0;
		int hours = 0;
		int minutes = 0;
		int seconds = 0;

		if (millis >= 31449600000L) // Years
		{
			years = (int) (millis / 31449600000L);
			millis -= (years * 31449600000L);
		}
		if (millis >= 2620799997L) // Months
		{
			months = (int) (millis / 2620799997L);
			millis -= (months * 2620799997L);
		}
		if (millis >= 604800000L) // Weeks
		{
			weeks = (int) (millis / 604800000L);
			millis -= (weeks * 604800000L);
		}
		if (millis >= 86400000L) // Days
		{
			days = (int) (millis / 86400000L);
			millis -= (days * 86400000L);
		}
		if (millis >= 3600000L) // Hours
		{
			hours = (int) (millis / 3600000L);
			millis -= (hours * 3600000L);
		}
		if (millis >= 60000L) // Minutes
		{
			minutes = (int) (millis / 60000L);
			millis -= (minutes * 60000L);
		}

		if (millisOriginalValue < 300000L) // Only display seconds if total time is less than 5 minutes
			seconds = (int)(millis / 1000L);

		String timeReturn = "";

		if (years != 0)
		{
			timeReturn += (" " + years + " year");
			if (years != 1)
				timeReturn += "s";
		}

		if (months != 0)
		{
			timeReturn += (" " + months + " month");
			if (months != 1)
				timeReturn += "s";
		}

		if (weeks != 0)
		{
			timeReturn += (" " + weeks + " week");
			if (weeks != 1)
				timeReturn += "s";
		}

		if (days != 0)
		{
			timeReturn += (" " + days + " day");
			if (days != 1)
				timeReturn += "s";
		}

		if (hours != 0)
		{
			timeReturn += (" " + hours + " hour");
			if (hours != 1)
				timeReturn += "s";
		}

		if (minutes != 0)
		{
			timeReturn += (" " + minutes + " minute");
			if (minutes != 1)
				timeReturn += "s";
		}

		if (seconds != 0)
		{
			timeReturn += (" " + seconds + " second");
			if (seconds != 1)
				timeReturn += "s";
		}

		return timeReturn.substring(1); // Get rid of first space
	}
}
