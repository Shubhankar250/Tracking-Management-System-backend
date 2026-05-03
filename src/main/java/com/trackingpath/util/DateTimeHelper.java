package com.trackingpath.util;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class DateTimeHelper {

	public static LocalDateTime utcToZone(LocalDateTime utcDateTime, String zoneId) {
		if (utcDateTime == null) {
			return null;
		}

		ZoneId targetZone = (zoneId == null || zoneId.isBlank()) ? ZoneOffset.UTC : ZoneId.of(zoneId);

		return utcDateTime.atZone(ZoneOffset.UTC) // treat input as UTC
				.withZoneSameInstant(targetZone).toLocalDateTime();
	}

	public static String utcToZone(String utcDateTimeStr, String zoneId) {
		if (utcDateTimeStr == null || utcDateTimeStr.isBlank()) {
			return null;
		}

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

		// String -> LocalDateTime
		LocalDateTime utcDateTime = LocalDateTime.parse(utcDateTimeStr, formatter);

		ZoneId targetZone = (zoneId == null || zoneId.isBlank()) ? ZoneOffset.UTC : ZoneId.of(zoneId);

		LocalDateTime converted = utcDateTime.atZone(ZoneOffset.UTC) // treat input as UTC
				.withZoneSameInstant(targetZone).toLocalDateTime();

		// return again in same string format
		return converted.format(formatter);
	}

	public static String localToIst(String utcDateTimeStr, String zoneId) {
		if (utcDateTimeStr == null || utcDateTimeStr.isBlank()) {
			return null;
		}

		ZoneId targetZone = (zoneId == null || zoneId.isBlank()) ? ZoneOffset.UTC : ZoneId.of(zoneId);

		// parse UTC string
		LocalDateTime utcDateTime = LocalDateTime.parse(utcDateTimeStr,
				DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

		// convert zone
		LocalDateTime targetDateTime = utcDateTime.atZone(ZoneOffset.UTC).withZoneSameInstant(targetZone)
				.toLocalDateTime();

		// return formatted string
		return targetDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
	}

	public static LocalDateTime stringToLocalDateTime(String stime) {
		if (stime == null || stime.isBlank()) {
			return null;
		}
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		LocalDateTime time = LocalDateTime.parse(stime, formatter);

		return time;

	}

	public static long getLocalUnixTime(long unixSeconds, String timezone) {

		ZoneId zoneId = ZoneId.of(timezone);

		Instant instant = Instant.ofEpochSecond(unixSeconds);

		ZonedDateTime userTime = instant.atZone(zoneId);

		return userTime.toEpochSecond();
	}

	public static long toUnix(LocalDateTime dateTime) {

		if (dateTime == null) {
			return 0L;
		}

		return dateTime.atZone(ZoneId.systemDefault()).toEpochSecond();
	}

	public static LocalDateTime convertToUserZone(LocalDateTime deviceDateTime, String deviceZoneId,
			String userZoneId) {
		
		if (deviceDateTime == null) {
			return null;
		}
		ZoneId userZone = (userZoneId == null || userZoneId.isBlank()) ? ZoneOffset.UTC : ZoneId.of(userZoneId);

		if (deviceZoneId == null || deviceZoneId.isBlank()) {
			return deviceDateTime.atZone(ZoneOffset.UTC).withZoneSameInstant(userZone).toLocalDateTime();
		}

		if (deviceZoneId.equalsIgnoreCase(userZoneId)) {
			return deviceDateTime;
		}

		ZoneId deviceZone = ZoneId.of(deviceZoneId);

		return deviceDateTime.atZone(deviceZone).withZoneSameInstant(userZone).toLocalDateTime();
	}
	public static LocalDateTime convertUserZoneToDeviceZone(
	        LocalDateTime userDateTime,
	        String userZoneId,
	        String deviceZoneId) {

	    if (userDateTime == null) {
	        return null;
	    }

	    ZoneId userZone = (userZoneId == null || userZoneId.isBlank())
	            ? ZoneOffset.UTC
	            : ZoneId.of(userZoneId);

	    ZoneId deviceZone = (deviceZoneId == null || deviceZoneId.isBlank())
	            ? ZoneOffset.UTC
	            : ZoneId.of(deviceZoneId);

	    if (userZone.equals(deviceZone)) {
	        return userDateTime;
	    }

	    return userDateTime
	            .atZone(userZone)
	            .withZoneSameInstant(deviceZone)
	            .toLocalDateTime();
	}
	
	
	
	
	public static LocalDateTime convertUtcToUser(LocalDateTime utcTime, String userZoneId) {

	    if (utcTime == null) return null;

	    ZoneId userZone = (userZoneId == null || userZoneId.isBlank())
	            ? ZoneOffset.UTC
	            : ZoneId.of(userZoneId);

	    return utcTime
	            .atZone(ZoneOffset.UTC)
	            .withZoneSameInstant(userZone)
	            .toLocalDateTime();
	}
}
