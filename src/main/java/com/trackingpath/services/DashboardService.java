package com.trackingpath.services;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

import org.json.JSONObject;
import org.springframework.stereotype.Service;

import com.trackingpath.dtos.DashboardSettingDTO;
import com.trackingpath.dtos.FleetUsesDTO;
import com.trackingpath.entities.LiveData;
import com.trackingpath.entities.Users;
import com.trackingpath.repositories.DeviceRepository;
import com.trackingpath.repositories.EventsRepository;
import com.trackingpath.repositories.ExpenseRepository;
import com.trackingpath.repositories.LiveDataRepository;
import com.trackingpath.repositories.MaintenanceRepository;
import com.trackingpath.util.Haversine;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardService {

    private final LiveDataRepository liveDataRepository;
    private final DeviceRepository deviceRepository;
    private final EventsRepository eventsRepository;
    private final MaintenanceRepository maintenanceRepository;
    private final ExpenseRepository expenseRepository;

    public DashboardSettingDTO getDashboardData(Users user) {

        DashboardSettingDTO dto = new DashboardSettingDTO();

        dto.setVehicleGpsMap(getDeviceGpsData(user));
        dto.setVehicleMovement(getVehicleMovement(user));
        dto.setHourlyDistanceData(getHourlyDistance(user));

        double totalDist = getTotalDistanceOfDay(user);
        long totalVehicles = deviceRepository.countByUserId(user.getAdminId());
        double avgDist = totalVehicles > 0 ? totalDist / totalVehicles : 0;

        dto.setTotalDistanceOfDay(round(totalDist));
        dto.setAverageDistancePerVehicle(round(avgDist));

        dto.setTotalAlerts((int) eventsRepository.countByUserId(user.getId()));
        
        dto.setAlertTypes(getAlertTypeCounts(user));

        dto.setMaintenanceDataMap(getMaintenanceData(user));
        dto.setTotalExpense(getTotalExpense());

        return dto;
    }

    private double round(double val) {
        return BigDecimal.valueOf(val)
            .setScale(2, RoundingMode.HALF_UP)
            .doubleValue();
    }
    public LinkedHashMap<String, Long> getDeviceGpsData(Users user) {

        List<LiveData> dataList = liveDataRepository.findLiveDataByUser(user.getAdminId());

        long online = 0, offline = 0, nodata = 0;

        long currentTime = System.currentTimeMillis() / 1000L;

        for (LiveData l : dataList) {

            LocalDateTime deviceTimeObj = l.getDevicetime();
            double lat = l.getLatitude();
            double lon = l.getLongitude();

            // Case 1: No coordinates at all -> No Data
            if (lat == 0.0 && lon == 0.0) {
                nodata++;
                continue;
            }

            // Case 2: deviceTime is NULL -> treat as OFFLINE
            if (deviceTimeObj == null) {
                offline++;
                continue;
            }

            long deviceTime = deviceTimeObj
                    .toInstant(ZoneOffset.UTC)
                    .getEpochSecond();

            // Online if last fix within 3 hours
            if (deviceTime > (currentTime - 10800)) {
                online++;
            } else {
                offline++;
            }
        }

        LinkedHashMap<String, Long> result = new LinkedHashMap<>();
        result.put("Total", online + offline + nodata);
        result.put("Online", online);
        result.put("Offline", offline);
        result.put("Nodata", nodata);

        return result;
    }


    public LinkedHashMap<String, Long> getVehicleMovement(Users user) {

        List<LiveData> list = liveDataRepository.findLiveDataByUser(user.getAdminId());

        long movement = 0, idle = 0, stopped = 0, nodata = 0;

        for (LiveData l : list) {

            double speed = l.getSpeed();
            double latitude = l.getLatitude();
            double longitude = l.getLongitude();
            String attributes = l.getAttributes();   // JSON string

            if (speed > 0.0) {

                movement++;

            } else {

                if (latitude == 0.0 && longitude == 0.0) {

                    nodata++;

                } else {

                    if (attributes != null && !attributes.isEmpty()) {

                        JSONObject obj = new JSONObject(attributes);

                        if (obj.has("ignition")) {

                            boolean ignition = obj.getBoolean("ignition");

                            if (ignition && speed == 0) {
                                idle++;
                            } else if (!ignition && speed == 0) {
                                stopped++;
                            }

                        } else {
                            stopped++; // ignition missing = consider as stopped
                        }

                    } else {
                        stopped++; // attributes empty = consider as stopped
                    }
                }
            }
        }

        LinkedHashMap<String, Long> data = new LinkedHashMap<>();
        data.put("Moving", movement);
        data.put("Idle", idle);
        data.put("Stopped", stopped);
        data.put("Nodata", nodata);

        return data;
    }
    public LinkedHashMap<Integer, Double> getHourlyDistance(Users user) {

        LinkedHashMap<Integer, Double> hourlyDistance = new LinkedHashMap<>();

        // Initialize all 24 hours (1–24)
        for (int h = 1; h <= 24; h++) {
            hourlyDistance.put(h, 0.0);
        }

        // Today's date range
        LocalDate today = LocalDate.now();
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.atTime(23, 59, 59);

        // Fetch live data using repository
        List<LiveData> dataList = liveDataRepository.findTodayData(start, end);

        // Convert to FleetUsesBean-style list
        List<FleetUsesDTO> data = new ArrayList<>();

        for (LiveData l : dataList) {

        	FleetUsesDTO bean = new FleetUsesDTO();
            bean.setLatitude(l.getLatitude());
            bean.setLongitude(l.getLongitude());

            // Convert deviceTime → epoch seconds
            long epoch = l.getDevicetime().toInstant(ZoneOffset.UTC).getEpochSecond();
            bean.setDeviceTime(epoch);

            data.add(bean);
        }

        // Calculate distances per hour
        if (data.size() > 1) {
            for (int i = 1; i < data.size(); i++) {

            	FleetUsesDTO prev = data.get(i - 1);
            	FleetUsesDTO curr = data.get(i);

                // Calculate distance using your Haversine class
                double distance = Haversine.getDistanceInKm(
                        prev.getLatitude(), prev.getLongitude(),
                        curr.getLatitude(), curr.getLongitude()
                );

                // Convert epoch to hour (1–24)
                LocalDateTime dateTime = LocalDateTime.ofEpochSecond(curr.getDeviceTime(), 0, ZoneOffset.UTC);

                int hour = dateTime.getHour() + 1;
                hour = Math.max(1, Math.min(hour, 24));

                double total = hourlyDistance.get(hour) + distance;
                double rounded = BigDecimal.valueOf(total)
                        .setScale(2, RoundingMode.HALF_UP)
                        .doubleValue();

                hourlyDistance.put(hour, rounded);
            }
        }

        return hourlyDistance;
    }
    public double getTotalDistanceOfDay(Users user) {

        LocalDate today = LocalDate.now();
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.atTime(23, 59, 59);

        List<LiveData> points = liveDataRepository.findPointsForToday(start, end);

        double totalDistance = 0.0;

        Double prevLat = null;
        Double prevLon = null;

        for (LiveData l : points) {

            double lat = l.getLatitude();
            double lon = l.getLongitude();

            if (prevLat != null && prevLon != null) {
                totalDistance += Haversine.getDistanceInKm(prevLat, prevLon, lat, lon);
            }

            prevLat = lat;
            prevLon = lon;
        }

        return totalDistance;
    }
    public LinkedHashMap<String, Integer> getAlertTypeCounts(Users user) {

        List<Object[]> results = eventsRepository.findAlertTypeCounts(user.getId());

        LinkedHashMap<String, Integer> map = new LinkedHashMap<>();

        for (Object[] row : results) {
            String alertType = (String) row[0];
            Integer count = ((Long) row[1]).intValue();   // COUNT returns Long
            map.put(alertType, count);
        }

        return map;
    }
    public LinkedHashMap<String, Long> getMaintenanceData(Users user) {
        Long due = maintenanceRepository.countDue(user.getId());
        Long overdue = maintenanceRepository.countOverdue(user.getId());

        LinkedHashMap<String, Long> map = new LinkedHashMap<>();
        map.put("Due", due != null ? due : 0L);
        map.put("Overdue", overdue != null ? overdue : 0L);

        return map;
    }
    
    public double getTotalExpense() {
    	String today = LocalDate.now().toString(); // "2025-12-11"

        Double total = expenseRepository.findTotalExpense(today);

        return total != null ? total : 0.0;
    }

}