package com.trackingpath.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trackingpath.dtos.LiveDataResponseDTO;
import com.trackingpath.entities.DeviceEntity;
import com.trackingpath.entities.LiveData;
import com.trackingpath.entities.Users;
import com.trackingpath.repositories.LiveDataRepository;
import com.trackingpath.util.DateTimeHelper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ApiService {

    private final LiveDataRepository liveDataRepository;
    private final ObjectMapper objectMapper;
    private final AuthenticationService authenticationService;

    // =========================
    // ✅ MAIN METHOD
    // =========================
    public List<LiveDataResponseDTO> getUserLiveData(String deviceId) {

        Users user = authenticationService.getCurrentUser();

        // ✅ Parse assigned device IDs
        List<Long> assignedDeviceIds = parseDeviceIds(user.getAssign_device_ids());

        if (assignedDeviceIds.isEmpty()) {
            return List.of();
        }

        List<LiveData> dataList;

        // ✅ Optional filter by uniqueid
        if (deviceId != null && !deviceId.isBlank()) {

            dataList = liveDataRepository
                    .findByDevice_IdInAndDevice_UniqueidAndDevicetimeIsNotNullOrderByDevicetimeDesc(
                            assignedDeviceIds, deviceId);

        } else {

            dataList = liveDataRepository
                    .findByDevice_IdInAndDevicetimeIsNotNullOrderByDevicetimeDesc(
                            assignedDeviceIds);
        }

        return dataList.stream()
                .map(ld -> convertToDTO(ld, user))
                .toList();
    }

    // =========================
    // ✅ DTO CONVERSION
    // =========================
    private LiveDataResponseDTO convertToDTO(LiveData ld, Users user) {

        DeviceEntity device = ld.getDevice();

        String deviceZone = device != null ? device.getDevicetimezone() : null;
        String userZone = user != null ? user.getTimezone() : "UTC";

        return LiveDataResponseDTO.builder()

                // ✅ Attributes
                .attributes(buildAttributes(ld.getAttributes()))

                .name(device != null ? device.getName() : null)
                .companyName("tms6")
                .deviceUniqueId(device != null ? device.getUniqueid() : null)

                // ✅ Timezone conversion
                .timestamp(DateTimeHelper.convertToUserZone(
                        ld.getServertime(), deviceZone, userZone))

                .serverTime(DateTimeHelper.convertToUserZone(
                        ld.getServertime(), deviceZone, userZone))

                .deviceTime(DateTimeHelper.convertToUserZone(
                        ld.getDevicetime(), deviceZone, userZone))

                .fixTime(DateTimeHelper.convertToUserZone(
                        ld.getFixtime(), deviceZone, userZone))

                .lastStatusUpdate(DateTimeHelper.convertToUserZone(
                        ld.getServertime(), deviceZone, userZone))

                // ✅ Boolean safe
                .valid(ld.getValid() != null ? ld.getValid() : false)

                // ✅ Numeric safe
                .latitude(ld.getLatitude() != null ? ld.getLatitude() : 0.0)
                .longitude(ld.getLongitude() != null ? ld.getLongitude() : 0.0)
                .altitude(ld.getAltitude() != null ? ld.getAltitude() : 0.0)
                .speed(ld.getSpeed() != null ? ld.getSpeed() : 0.0)
                .course(ld.getCourse() != null ? ld.getCourse() : 0.0)

                // ✅ Address safe
                .address(ld.getAddress() != null && !ld.getAddress().isBlank()
                        ? ld.getAddress()
                        : null)

                // ✅ Accuracy safe
                .accuracy(ld.getAccuracy() != null
                        ? Double.valueOf(ld.getAccuracy())
                        : 0.0)

                .build();
    }

    // =========================
    // ✅ ATTRIBUTES MAPPING
    // =========================
    private Map<String, Object> buildAttributes(String attributesJson) {

        Map<String, Object> raw = new HashMap<>();
        Map<String, Object> result = new HashMap<>();

        try {
            if (attributesJson != null) {
                raw = objectMapper.readValue(attributesJson, Map.class);
            }
        } catch (Exception e) {
            System.out.println("Attribute parse error: " + e.getMessage());
        }

        result.put("power", getDouble(raw, "power"));
        result.put("ignition", getBoolean(raw, "ignition"));
        result.put("charge", getBoolean(raw, "charge"));
        result.put("batteryLevel", getDouble(raw, "battery"));
        result.put("ac", getObject(raw, "ac"));
        result.put("door", getObject(raw, "door"));
        result.put("panic", getObject(raw, "panic"));
        result.put("alarm", getObject(raw, "alarm"));
        result.put("motion", getBoolean(raw, "motion"));
        result.put("totalDistance", getDouble(raw, "totalDistance"));
        result.put("todayDistance", getDouble(raw, "todayDistance"));

        return result;
    }

    // =========================
    // ✅ HELPER METHODS
    // =========================

    private Double getDouble(Map<String, Object> map, String key) {
        Object val = map.get(key);
        if (val == null) return 0.0;
        try {
            return Double.valueOf(val.toString());
        } catch (Exception e) {
            return 0.0;
        }
    }

    private Boolean getBoolean(Map<String, Object> map, String key) {
        Object val = map.get(key);
        if (val == null) return false;
        return Boolean.valueOf(val.toString());
    }

    private Object getObject(Map<String, Object> map, String key) {
        return map.getOrDefault(key, null);
    }

    // =========================
    // ✅ PARSE DEVICE IDS
    // =========================
    private List<Long> parseDeviceIds(String ids) {

        if (ids == null || ids.isBlank()) {
            return List.of();
        }

        return List.of(ids.split(","))
                .stream()
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Long::valueOf)
                .toList();
    }
}