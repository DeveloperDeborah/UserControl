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
		if (millis < SECOND_MILLISECONDS)
			return "Less than one second";
		long millisOriginalValue = millis;
		int years = 0;
		int months = 0;
		int weeks = 0;
		int days = 0;
		int hours = 0;
		int minutes = 0;
		int seconds = 0;

		if (millis >= YEAR_MILLISECONDS) // Years
		{
			years = (int) (millis / YEAR_MILLISECONDS);
			millis -= (years * YEAR_MILLISECONDS);
		}
		if (millis >= MONTH_MILLISECONDS) // Months
		{
			months = (int) (millis / MONTH_MILLISECONDS);
			millis -= (months * MONTH_MILLISECONDS);
		}
		if (millis >= WEEK_MILLISECONDS) // Weeks
		{
			weeks = (int) (millis / WEEK_MILLISECONDS);
			millis -= (weeks * WEEK_MILLISECONDS);
		}
		if (millis >= DAY_MILLISECONDS) // Days
		{
			days = (int) (millis / DAY_MILLISECONDS);
			millis -= (days * DAY_MILLISECONDS);
		}
		if (millis >= HOUR_MILLISECONDS) // Hours
		{
			hours = (int) (millis / HOUR_MILLISECONDS);
			millis -= (hours * HOUR_MILLISECONDS);
		}
		if (millis >= MINUTE_MILLISECONDS) // Minutes
		{
			minutes = (int) (millis / MINUTE_MILLISECONDS);
			millis -= (minutes * MINUTE_MILLISECONDS);
		}

		if (millisOriginalValue < 5 * MINUTE_MILLISECONDS) // Only display seconds if total time is less than 5 minutes
			seconds = (int)(millis / SECOND_MILLISECONDS);

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

	private static final long YEAR_MILLISECONDS = 31536000000L; // 365 days
	private static final long MONTH_MILLISECONDS = 2628000000L; // 1/12 of a year
	private static final long WEEK_MILLISECONDS = 604800000L;
	private static final long DAY_MILLISECONDS = 86400000L;
	private static final long HOUR_MILLISECONDS = 3600000L;
	private static final long MINUTE_MILLISECONDS = 60000L;
	private static final long SECOND_MILLISECONDS = 1000L;
}
