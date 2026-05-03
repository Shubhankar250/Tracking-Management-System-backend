package com.trackingpath.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.TimeZone;




public class TimeZoneConverter {
	public  static String getLocalTime(Date date,String userTimezone){			
			DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			format.setTimeZone(TimeZone.getTimeZone(userTimezone));
			String formatted = format.format(date);
			return formatted;		
	}
	public  static String getLocalTime(String date,String userTimezone) throws ParseException{	
		Date dt=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(date); 
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		format.setTimeZone(TimeZone.getTimeZone("GMT"));
		String formatted = format.format(dt);
		return formatted;	
		
}
	public  static long getLocalUnixTime(long unixTimestamp,String userTimezone) {	
		
			Date date = new Date(unixTimestamp*1000);
			DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			format.setTimeZone(TimeZone.getTimeZone(userTimezone));
			String formatted = format.format(date);
			try {
				return format.parse(formatted).getTime();
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return 0;
		
		
	}
	
	public static long convertAsiaKolkataToUTC(String asiaKolkataDateTime) {
        // Define the time zone for Asia/Kolkata
        ZoneId kolkataZoneId = ZoneId.of("Asia/Kolkata");

        // Parse the input datetime string into a ZonedDateTime object assuming the input format is "yyyy-MM-dd HH:mm:ss"
        ZonedDateTime kolkataDateTime = ZonedDateTime.parse(asiaKolkataDateTime, DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss").withZone(kolkataZoneId));

        // Convert it to UTC by converting the zone to UTC
        ZonedDateTime utcDateTime = kolkataDateTime.withZoneSameInstant(ZoneId.of("UTC"));

        // Return the time in milliseconds since the Unix epoch (1970-01-01T00:00:00Z)
        return utcDateTime.toInstant().toEpochMilli();
    }
	 public static long convertAsiaKolkataToUTCshare(String asiaKolkataDateTime) {
	        if (asiaKolkataDateTime == null || asiaKolkataDateTime.isEmpty()) return 0L;

	        // Format matches DB string
	        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	        // Parse string to LocalDateTime
	        LocalDateTime localDateTime = LocalDateTime.parse(asiaKolkataDateTime, formatter);

	        // Attach Asia/Kolkata timezone
	        ZonedDateTime kolkataDateTime = localDateTime.atZone(ZoneId.of("Asia/Kolkata"));

	        // Convert to UTC
	        ZonedDateTime utcDateTime = kolkataDateTime.withZoneSameInstant(ZoneId.of("UTC"));

	        return utcDateTime.toInstant().toEpochMilli();
	    }
	public static void main(String[] args) throws ParseException {
		/*Date date = new Date(1465748402L*1000);
		DateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		format.setTimeZone(TimeZone.getTimeZone("IST"));
		String formatted = format.format(date);
		System.out.println(formatted);
		format.setTimeZone(TimeZone.getTimeZone("Asia/Kathmandu"));
		formatted = format.format(date);
		System.out.println(formatted);*/
		
		//TimeZoneConverter.getLocalTime(1465748887);
		//System.out.println(getLocalTime("2020-02-05 07:00:00","GMT"));
		check();
	}
	public static void check() {
		   
	     //Date will return local time in Java  
	     Date localTime = new Date();
	   
	     //creating DateFormat for converting time from local timezone to GMT
	     DateFormat converter = new SimpleDateFormat("dd/MM/yyyy:HH:mm:ss");
	   
	     //getting GMT timezone, you can get any timezone e.g. UTC
	     converter.setTimeZone(TimeZone.getTimeZone("GMT"));
	   
			/*
			 * System.out.println("local time : " + localTime);;
			 * System.out.println("time in GMT : " + converter.format(localTime));
			 */
	   
	    }

	//Read more: https://javarevisited.blogspot.com/2012/04/how-to-convert-local-time-to-gmt-in.html#ixzz6D8fdPvG7
}
