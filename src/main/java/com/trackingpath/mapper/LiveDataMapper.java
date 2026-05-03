package com.trackingpath.mapper;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.trackingpath.dtos.DeviceSettingCustomBean;
import com.trackingpath.dtos.LiveDataBean;
import com.trackingpath.dtos.LiveDataProjection;
import com.trackingpath.entities.Users;
import com.trackingpath.repositories.DeviceRepository;
import com.trackingpath.util.AttributeHelper;
import com.trackingpath.util.DateTimeHelper;
import com.trackingpath.util.DateTimeUtil;

@Component
public class LiveDataMapper {

	@Autowired
	private AttributeHelper attributeHelper;
	@Autowired
	private DeviceRepository deviceRepository;
	public LiveDataBean mapToBean(LiveDataProjection p, Users user) {
		String devicetimezone = deviceRepository.findDeviceTimeZone(p.getDeviceId());
		LiveDataBean bean = new LiveDataBean();
		bean.setUniqueid(p.getUniqueid());


		bean.setDevice_id(p.getDeviceId());
		bean.setDevice_name(p.getDeviceName());
		bean.setGroup_id(p.getGroupId());
		bean.setGroup_name(p.getGroupName());
		bean.setGps_status(p.getGpsStatus());


		bean.setDevicetime(DateTimeHelper.convertToUserZone(p.getDeviceTime(), devicetimezone, user.getTimezone()));
		bean.setServertime(DateTimeHelper.utcToZone(p.getServerTime(), user.getTimezone()));
		LocalDateTime deviceTime = p.getDeviceTime();

       	String status = "Offline";

       	if (deviceTime != null) {

       	    ZoneId zone;

       	    if (devicetimezone == null || devicetimezone.isBlank()) {
       	        zone = ZoneOffset.UTC; // fallback
       	    } else {
       	        zone = ZoneId.of(devicetimezone);
       	    }

       	    // convert device time -> UTC instant
       	    Instant deviceInstant = deviceTime.atZone(zone).toInstant();

       	    long diffSeconds = Duration.between(deviceInstant, Instant.now()).getSeconds();

       	    if (diffSeconds <= 300) {
       	        status = "online";
       	    }
       	}

          	bean.setStatus(status);
		long now = DateTimeHelper.getLocalUnixTime(System.currentTimeMillis() / 1000L, user.getTimezone());

		if (p.getLastMovementTime() != null) {
			bean.setLastmovementtime(
					DateTimeUtil.getTimeDiffInDays((now - DateTimeHelper.toUnix(p.getLastMovementTime())) * 1000));
		}

		if (p.getLastIdleTime() != null) {
			bean.setLastidletime(
					DateTimeUtil.getTimeDiffInDays((now - DateTimeHelper.toUnix(p.getLastIdleTime())) * 1000));
		}

		bean.setLatitude(p.getLatitude());
		bean.setLongitude(p.getLongitude());
		bean.setAltitude(p.getAltitude());
		bean.setSpeed(p.getSpeed() != null ? p.getSpeed().longValue() : 0L);
		bean.setCourse(p.getCourse());
		bean.setAddress(p.getAddress());
		bean.setAttributes(p.getAttributes());
        bean.setChannelNo(p.getChannelNo());
		// ---- Attributes JSON ----
		if (p.getAttributes() != null && !p.getAttributes().isEmpty()) {

			JSONObject obj = new JSONObject(p.getAttributes());

			if (obj.has("ignition"))
				bean.setIgnition(obj.getBoolean("ignition"));
			if (obj.has("power"))
				bean.setPower(obj.getDouble("power"));
			if (obj.has("sat"))
				bean.setGps_status(String.valueOf(obj.getLong("sat")));
			if (obj.has("motion"))
				bean.setMotion(obj.getBoolean("motion"));
			if (obj.has("distance"))
				bean.setDistance(obj.getDouble("distance"));
			if (obj.has("battery"))
				bean.setBattery(obj.getDouble("battery"));
		}

		// ---- Device Settings ----
		DeviceSettingCustomBean deviceSetting = new DeviceSettingCustomBean();
		deviceSetting.setIcon_type(p.getIconType());
		deviceSetting.setMoving_icon_color(p.getMovingIconColor());
		deviceSetting.setStopped_icon_color(p.getStoppedIconColor());
		deviceSetting.setOffline_icon_color(p.getOfflineIconColor());
		deviceSetting.setEngine_idle_color(p.getEngineIdleColor());
		deviceSetting.setImg_icon_name(p.getImgIconName());
		deviceSetting.setImg_icon_type(p.getImgIconType());

		bean.setDeviceSetting(deviceSetting);

		// ---- Sensor Processing ----
		bean.setResultantSensorBean(attributeHelper.getAttributeHelperData(bean));

		return bean;
	}
}
