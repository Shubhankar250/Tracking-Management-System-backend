package com.trackingpath.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class DateTimeUtil {

	static DateFormat startDateformat = new SimpleDateFormat("yyyy-MM-dd 00:00:01");
	static DateFormat endDateformat = new SimpleDateFormat("yyyy-MM-dd 23:59:59");
	static DateFormat MYSQLdateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	static DateFormat  customdateFormat = new SimpleDateFormat("MMMM,dd,yyyy");



	public static Date addDays(Date date, int days) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.set(Calendar.MINUTE, 23);
		c.set(Calendar.SECOND, 59);
		c.add(Calendar.DATE, days);
		return new Date(c.getTimeInMillis());
	}

	public static Date subtractDays(Date date, int days) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.add(Calendar.DATE, -days);
		return new Date(c.getTimeInMillis());
	}

	public static long getTodayStartTime(String timezone) {
		try {
			long time = MYSQLdateformat.parse(startDateformat.format(new Date())).getTime() / 1000l;

			return time + DateTimeUtil.getTimeDifferenceFromGMT(timezone);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;

	}

	public static long getTodayEndTime(String timezone) {
		try {
			long time = MYSQLdateformat.parse(endDateformat.format(new Date())).getTime() / 1000l;

			return time + DateTimeUtil.getTimeDifferenceFromGMT(timezone);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;

	}

	public static long convertUnixTime(String date, String userTimezone) {
		Date localDate;
		try {
			MYSQLdateformat.setTimeZone(TimeZone.getTimeZone(userTimezone));
			localDate = MYSQLdateformat.parse(date);
			long epoch = localDate.getTime();
			return epoch / 1000l;
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;

	}
	/*
	 * public static String getLocalTimezoneDate(String date, String userTimezone) {
	 * Date localDate; try {
	 * MYSQLdateformat.setTimeZone(TimeZone.getTimeZone(userTimezone)); localDate =
	 * MYSQLdateformat.parse(date); return MYSQLdateformat.format(localDate); }
	 * catch (ParseException e) { // TODO Auto-generated catch block
	 * e.printStackTrace(); } return null;
	 * 
	 * 
	 * }
	 */

	public static String convertUserTimeToUTCForshare(String datetime, String userTimezone) {
	    try {
	        DateFormat formatUser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX");
	        //formatUser.setTimeZone(TimeZone.getTimeZone(userTimezone));

	        DateFormat formatUTC = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	        formatUTC.setTimeZone(TimeZone.getTimeZone("UTC"));

	        Date date = formatUser.parse(datetime);
	        return formatUTC.format(date);

	    } catch (ParseException e) {
	        e.printStackTrace();
	    }
	    return null;
	}
	public static String convertUserTimeToUTC(String datetime, String userTimezone) {
        // String formatted = formatUTC.format(formatUser.parse(dt));
        try {
            DateFormat formatUTC = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            formatUTC.setTimeZone(TimeZone.getTimeZone("UTC"));

            DateFormat formatUser = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            formatUser.setTimeZone(TimeZone.getTimeZone(userTimezone));
            return formatUTC.format(formatUser.parse(datetime));
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }


	public static String convertUTCToUserTime(String datetime, String userTimezone) {
		// String formatted = formatUTC.format(formatUser.parse(dt));
		try {
			DateFormat formatUser = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			formatUser.setTimeZone(TimeZone.getTimeZone(userTimezone));

			DateFormat formatUTC = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			formatUTC.setTimeZone(TimeZone.getTimeZone("UTC"));

			// System.out.println("time:>>"+formatUser.format(formatUTC.parse(datetime)));
			return formatUser.format(formatUTC.parse(datetime));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	
	public static String convertUTCToUserTimeCustomFormat(long  unixTimestamp, String userTimezone) {
		Date date = new Date(unixTimestamp * 1000);
		DateFormat format = new SimpleDateFormat("MMMM,dd yyyy", Locale.ENGLISH);
		format.setTimeZone(TimeZone.getTimeZone(userTimezone));
		String formatted = format.format(date);
		return formatted;
	}

	public static long getTimeDifferenceFromGMT(String timezone) {
		TimeZone tz1 = TimeZone.getTimeZone("GMT");
		TimeZone tz2 = TimeZone.getTimeZone(timezone);
		long timeDifference = tz1.getRawOffset() - tz2.getRawOffset() + tz1.getDSTSavings() - tz2.getDSTSavings();

		return timeDifference / 1000l;
	}

	public static String getTimeDiffInDays(long diffInMilliSec) {
		long seconds = TimeUnit.MILLISECONDS.toSeconds(diffInMilliSec) % 60;
		long minutes = TimeUnit.MILLISECONDS.toMinutes(diffInMilliSec) % 60;
		long hours = TimeUnit.MILLISECONDS.toHours(diffInMilliSec) % 24;
		long days = TimeUnit.MILLISECONDS.toDays(diffInMilliSec) % 365;
		long years = TimeUnit.MILLISECONDS.toDays(diffInMilliSec) / 365l;
		StringBuilder sb = new StringBuilder();
		if (years > 0) {
			sb.append(years + "y ");
		}
		if (days > 0) {
			sb.append(days + "d ");
		}
		if (hours > 0) {
			sb.append(hours + "h ");
		}
		if (minutes > 0) {
			sb.append(minutes + "min ");
		}
		if (seconds > 0) {
			sb.append(seconds + "s ");
		}
		return sb.toString();
	}
	
	public static String getTimeDiffInDaysShort(long diffInMilliSec) {
		long seconds = TimeUnit.MILLISECONDS.toSeconds(diffInMilliSec) % 60;
		long minutes = TimeUnit.MILLISECONDS.toMinutes(diffInMilliSec) % 60;
		long hours = TimeUnit.MILLISECONDS.toHours(diffInMilliSec) % 24;
		long days = TimeUnit.MILLISECONDS.toDays(diffInMilliSec) % 365;
		long years = TimeUnit.MILLISECONDS.toDays(diffInMilliSec) / 365l;
		StringBuilder sb = new StringBuilder();
		if (years > 0) {
			sb.append(years + " Y ");
		}
		if (days > 0) {
			sb.append(days + " D ");
		}
		if (hours > 0) {
			sb.append(hours + " Hr ");
		}
		if (minutes > 0) {
			sb.append(minutes + " Min ");
		}
		if (seconds > 0) {
			sb.append(seconds + " Sec ");
		}
		return sb.toString();
	}
	public static long getLocalUnixTime(long unixTimestamp, String userTimezone) {
		Date date = new Date(unixTimestamp * 1000);
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		format.setTimeZone(TimeZone.getTimeZone(userTimezone));
		String formatted = format.format(date);
		try {
			return format.parse(formatted).getTime() / 1000l;
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}
	
	public static long getUTCUnixTime(long unixTimestamp) {
		Date date = new Date(unixTimestamp * 1000);
		
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		format.setTimeZone(TimeZone.getTimeZone("UTC"));
		String formatted = format.format(date);
		try {
			return format.parse(formatted).getTime() / 1000l;
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}

	public static String getLocalTimeyyyyMMdd(long unixTimestamp, String userTimezone) {
		Date date = new Date(unixTimestamp * 1000);
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
		format.setTimeZone(TimeZone.getTimeZone(userTimezone));
		String formatted = format.format(date);
		return formatted;
	}

	public static String getLocalTime(long unixTimestamp, String userTimezone) {
		if (unixTimestamp != 0) {
			Date date = new Date(unixTimestamp);
			DateFormat format = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss", Locale.ENGLISH);
			format.setTimeZone(TimeZone.getTimeZone(userTimezone));
			String formatted = format.format(date);
			return formatted;
		}
		return "--";
	}

	public static String getLocalDate(long unixTimestamp, String userTimezone) {
		if (unixTimestamp != 0) {
			Date date = new Date(unixTimestamp * 1000);
			DateFormat format = new SimpleDateFormat("dd-MMM-yyyy", Locale.ENGLISH);
			format.setTimeZone(TimeZone.getTimeZone(userTimezone));
			String formatted = format.format(date);
			return formatted;
		}
		return "--";
	}
	public static String CurrentDateTime()
	{
		  DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");  
		   LocalDateTime now = LocalDateTime.now();  
		   
		return dtf.format(now).toString();  
	}

	public static void main(String[] args) {
		/*
		 * String datetime = "2021-09-30 17:42:00"; String timezone = "Asia/Kolkata";
		 * convertUTCToUserTime(datetime,timezone);
		 */
		//System.out.println(getLocalTime(1669040387l, "UTC"));
		//System.out.println(getLocalDate(1669040387l, "UTC"));
		System.out.println(DateTimeUtil.convertUserTimeToUTC("2023-08-24 00:00:00","Asia/Kolkata"));

	}
	public static String getLocalTimeDDMMYYYY(long epochMilli, String timezone) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss"); // <-- changed format
        sdf.setTimeZone(TimeZone.getTimeZone(timezone));
        return sdf.format(new Date(epochMilli));
 }
}
