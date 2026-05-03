
package com.trackingpath.services;
import com.trackingpath.dtos.DailySummaryReportDTO;
import com.trackingpath.dtos.DeviceBean;
import com.trackingpath.dtos.DistanceReportDto;
import com.trackingpath.dtos.EventDataBean;
import com.trackingpath.dtos.MovementReportProjection;
import com.trackingpath.dtos.OverspeedReportDto;
import com.trackingpath.dtos.ReportDTO;
import com.trackingpath.entities.ReportEntity;
import com.trackingpath.entities.ReportLogEntity;
import com.trackingpath.entities.Users;
import com.trackingpath.mapper.ReportMapper;
import com.trackingpath.repositories.DailySummaryReportRepository;
import com.trackingpath.repositories.DeviceRepository;
import com.trackingpath.repositories.DistanceReportRepository;
import com.trackingpath.repositories.MovementReportRepository;
import com.trackingpath.repositories.ReportLogRepository;
import com.trackingpath.repositories.ReportMasterRepository;
import com.trackingpath.repositories.ReportRepository;
import com.trackingpath.repositories.UserRepository;
import com.trackingpath.util.DateTimeHelper;
import com.trackingpath.util.DateTimeUtil;
import com.trackingpath.util.Haversine;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class ReportService {
	
	private final DeviceRepository deviceRepository;

    private final ReportRepository reportRepository;
    private final  ReportLogRepository reportLogRepository;
    private final UserRepository userRepository; 
    private final MovementReportRepository movementReportRepository;

    private final ReportMapper reportMapper;
    private final ReportMasterRepository reportMasterRepository;
    private static DecimalFormat decimal_format = new DecimalFormat("0.00");
    @Autowired
    DistanceReportRepository distanceReportRepository;
    
    @Autowired
    DailySummaryReportRepository dailySummaryReportRepository;
    public boolean insertReportDetails(ReportDTO reportBean, Users user) {

        ReportEntity entity = reportMapper.toEntity(reportBean, user);
        return reportRepository.save(entity).getId() != null;
    }

    public Page<ReportDTO> getReportData(Users user, String search, Pageable pageable) {
    	String searchParam = (search != null ? search.trim() : "");
    	
        return reportRepository.getReportData(searchParam, pageable)
                .map(reportMapper::mapWithPeriodDates);
    }

    public Page<ReportLogEntity> getReportDataLog(String search, Pageable pageable) {
    	String searchParam = (search != null ? search.trim() : "");
        return reportLogRepository.findAll(searchParam, pageable);
    }
    
   
    public boolean deleteReport(Long id, Users user) {
        if (!userRepository.existsById(user.getId())) {
            return false;
        }
        reportRepository.deleteById(id);
        return true;
    }

    
    public boolean deleteReportLog(Long id, Users user) {
        if (!userRepository.existsById(user.getId())) {
            return false;
        }
        reportLogRepository.deleteById(id);
        return true;
    }
    public ReportDTO getReportById(Long id) {

        ReportEntity entity = reportRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Report not found"));

        ReportDTO bean = new ReportDTO();

        bean.setId(entity.getId());
        bean.setTitle(entity.getTitle());
        bean.setType(entity.getType());
        bean.setFormat(entity.getFormat());
        bean.setPeriod(entity.getPeriod());
        bean.setEmails(entity.getEmails());
        bean.setSpeed_limit(entity.getSpeed_limit());
        bean.setStops(entity.getStops());
        bean.setDaily(entity.getDaily());
        bean.setWeekly(entity.getWeekly());
        bean.setMonthly(entity.getMonthly());

        // devices
        if (entity.getDevices() != null && !entity.getDevices().isEmpty()) {
            bean.setDevices(Arrays.stream(entity.getDevices().split(","))
                    .map(String::trim)
                    .map(Long::parseLong)
                    .toList());
        }

        // geofences
        if (entity.getGeofences() != null && !entity.getGeofences().isEmpty()) {
            bean.setGeofences(Arrays.stream(entity.getGeofences().split(","))
                    .map(String::trim)
                    .map(Long::parseLong)
                    .toList());
        }

        // skip columns
        if (entity.getSkip_column() != null && !entity.getSkip_column().isEmpty()) {
            bean.setSkip_column(Arrays.stream(entity.getSkip_column().split(","))
                    .map(String::trim)
                    .toList());
        }

        return bean;
    }
    @Transactional
    public boolean updateReportData(ReportDTO dto, Users user) {
        if (user == null) return false;

        return reportRepository.findByIdAndAdminId(dto.getId(), user.getAdminId())
                .map(entity -> {

                    // Update simple fields
                    if (dto.getTitle() != null) entity.setTitle(dto.getTitle());
                    if (dto.getType() != null) entity.setType(dto.getType());
                    if (dto.getFormat() != null) entity.setFormat(dto.getFormat());
                    if (dto.getPeriod() != null) entity.setPeriod(dto.getPeriod());
                    if (dto.getEmails() != null) entity.setEmails(dto.getEmails());
                    if (dto.getSpeed_limit() != null) entity.setSpeed_limit(dto.getSpeed_limit());
                    if (dto.getStops() != null) entity.setStops(dto.getStops());
                    if (dto.getDaily() != null) entity.setDaily(dto.getDaily());
                    if (dto.getWeekly() != null) entity.setWeekly(dto.getWeekly());
                    if (dto.getMonthly() != null) entity.setMonthly(dto.getMonthly());

                    // Convert list fields to comma-separated strings
                    if (dto.getDevices() != null) {
                        entity.setDevices(dto.getDevices().stream()
                                .map(String::valueOf)
                                .collect(Collectors.joining(",")));
                    }
                    if (dto.getGeofences() != null) {
                        entity.setGeofences(dto.getGeofences().stream()
                                .map(String::valueOf)
                                .collect(Collectors.joining(",")));
                    }
                    if (dto.getSkip_column() != null) {
                        entity.setSkip_column(String.join(",", dto.getSkip_column()));
                    }

                    // JPA auto-saves at transaction commit
                    return true;
                })
                .orElse(false);
    }
    public List<String> getColumnByReportType(String reportType, Users user) {

        return reportMasterRepository
                .findByReportType(reportType)
                .map(r -> Arrays.asList(r.getReportColumn().split(",")))
                .orElse(Collections.emptyList());
    }
    public List<EventDataBean> getMovementReport(
            String from_date,
            String to_date,
            List<Long> deviceIds,
            List<Long> geofences,
            String speed,          
            Users user) {

        String startDateTime = from_date + " 00:00:00";
        String endDateTime = to_date + " 23:59:59";

        // ✅ User → UTC
        String startTime = DateTimeUtil.convertUserTimeToUTC(startDateTime, user.getTimezone());
        String endTime = DateTimeUtil.convertUserTimeToUTC(endDateTime, user.getTimezone());

        // ✅ Movement data
        List<MovementReportProjection> rows =
                movementReportRepository.getMovementData(
                        startTime,
                        endTime,
                        deviceIds,
                        speed != null && !speed.isEmpty() ? Double.parseDouble(speed) : null
                );

        // ✅ Device Timezones
        List<Object[]> deviceDetails = deviceRepository.findDeviceTimeZoneByIds(deviceIds);

        Map<Long, String> deviceTimezoneMap = new HashMap<>();
        for (Object[] obj : deviceDetails) {
            deviceTimezoneMap.put((Long) obj[0], (String) obj[1]);
        }

        return mapToEventDataBean(rows,user, deviceTimezoneMap);
    }
    private List<EventDataBean> mapToEventDataBean(
            List<MovementReportProjection> rows,          
            Users user,
            Map<Long, String> deviceTimezoneMap) {

        List<EventDataBean> list = new ArrayList<>();

        double totalDistance = 0;
        double lastLat = 0, lastLng = 0;      
        for (int i = 0; i < rows.size(); i++) {

            MovementReportProjection r = rows.get(i);

            EventDataBean bean = new EventDataBean();

            // ✅ Distance
            if (i > 0) {
                totalDistance += Haversine.getDistanceInKm(
                        lastLat, lastLng,
                        r.getLatitude(), r.getLongitude());
            }

            // ✅ Basic fields
            bean.setAddress(r.getAddress());
            bean.setAltitude(r.getAltitude());
            bean.setAttributes(r.getAttributes());
            bean.setCourse(r.getCourse());
            bean.setLatitude(r.getLatitude());
            bean.setLongitude(r.getLongitude());
            bean.setSpeed(r.getSpeed());
            bean.setValid(String.valueOf(r.getValid()));
            bean.setFuelLevel(r.getFuellevel());

            String deviceZone = deviceTimezoneMap.get(r.getDeviceid());

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

         bean.setDeviceTime(
                 DateTimeHelper.convertToUserZone(
                         Instant.ofEpochSecond(r.getDevicetime())
                                 .atZone(ZoneOffset.UTC)
                                 .toLocalDateTime(),
                         deviceZone,
                         user.getTimezone()
                 ).format(formatter)
         );

         bean.setFixTime(
                 DateTimeHelper.convertToUserZone(
                         Instant.ofEpochSecond(r.getFixtime())
                                 .atZone(ZoneOffset.UTC)
                                 .toLocalDateTime(),
                         deviceZone,
                         user.getTimezone()
                 ).format(formatter)
         );

         bean.setServerTime(
                 DateTimeHelper.convertToUserZone(
                         Instant.ofEpochSecond(r.getServertime())
                                 .atZone(ZoneOffset.UTC)
                                 .toLocalDateTime(),
                         deviceZone,
                         user.getTimezone()
                 ).format(formatter)
         );
    
            bean.setDistance(
                    Double.parseDouble(decimal_format.format(totalDistance)));

            // ✅ Device details
            DeviceBean device = new DeviceBean();
            device.setDevice_id(r.getDeviceid());
            device.setName(r.getName());
            device.setModel(r.getDevice_model());
            bean.setDetails(device);

            // ✅ Attributes
            if (r.getAttributes() != null) {
                JSONObject obj = new JSONObject(r.getAttributes());
                bean.setGps_satellite(obj.optInt("sat", 0));
                bean.setGsm(obj.optInt("gsm", 0));
                bean.setIgnition(obj.optBoolean("ignition", false));
            }

            lastLat = r.getLatitude();
            lastLng = r.getLongitude();

            list.add(bean);
        }

        return list;
    }
    public List<OverspeedReportDto> getOverspeedReport(
            String from_date,
            String to_date,
            List<Long> deviceIds,
            String speed,
            Users user) {

        List<OverspeedReportDto> list = new ArrayList<>();

        if (deviceIds == null || deviceIds.isEmpty()) {
            return list;
        }

        // ✅ IST → UTC conversion
        String startDateTime = from_date + " 00:00:00";
        String endDateTime = to_date + " 23:59:59";

        String startTime = DateTimeUtil.convertUserTimeToUTC(startDateTime, user.getTimezone());
        String endTime = DateTimeUtil.convertUserTimeToUTC(endDateTime, user.getTimezone());

        long speedLimit = 40;
        if (speed != null && !speed.isEmpty()) {
            try {
                speedLimit = Long.parseLong(speed);
            } catch (Exception ignored) {}
        }

       

        // ✅ DB Call
        List<Object[]> rows = movementReportRepository.getOverspeedData(
                user.getAdminId(),
                deviceIds,
                startTime,
                endTime,
                speedLimit
        );

      

        for (Object[] row : rows) {

            double speedVal = ((Number) row[0]).doubleValue();
            String name = (String) row[1];
            String model = (String) row[2];
            String address = (String) row[3];
            long deviceTime = ((Number) row[4]).longValue();
            double lat = ((Number) row[5]).doubleValue();
            double lng = ((Number) row[6]).doubleValue();          
            OverspeedReportDto bean = new OverspeedReportDto();

            bean.setAddress(address);

            bean.setDeviceTime(
                    DateTimeHelper.convertToUserZone(
                            Instant.ofEpochSecond(deviceTime)
                                    .atZone(ZoneOffset.UTC)
                                    .toLocalDateTime(),
                            "UTC",
                            user.getTimezone()
                    ).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            );

            bean.setSpeed(speedVal);
            bean.setLatitude(lat);
            bean.setLongitude(lng);
            bean.setDeviceName(name);
            bean.setDeviceModel(model);
            bean.setSpeedLimit(speedLimit);

            list.add(bean);
        }

        return list;
    }
    public List<DistanceReportDto> distanceReport(
            long deviceId,
            String startTime,
            String endTime,
            Users user) {
    	String startDateTime = startTime + " 00:00:00";
        String endDateTime = endTime + " 23:59:59";
        List<Object[]> rows =
                distanceReportRepository.getDistanceData(deviceId, startDateTime, endDateTime);

        List<DistanceReportDto> list = new ArrayList<>();

        double lat = 0;
        double lon = 0;
        double totalDistance = 0;

        DistanceReportDto bean = new DistanceReportDto();

        boolean first = true;

        for (Object[] row : rows) {

            String address = (String) row[0];
            double currentLat = ((Number) row[2]).doubleValue();
            double currentLon = ((Number) row[3]).doubleValue();
            String deviceName = (String) row[4];

            if (first) {
                bean.setStartPoint(address);
                lat = currentLat;
                lon = currentLon;
                bean.setName(deviceName);
                first = false;
            } else {

                totalDistance += Haversine.getDistanceInKm(
                        lat, lon,
                        currentLat, currentLon
                );

                lat = currentLat;
                lon = currentLon;

                bean.setEndPoint(address);
            }
        }

        bean.setDistance(decimal_format.format(totalDistance));

        bean.setStime(DateTimeUtil.convertUTCToUserTime(startTime, user.getTimezone()));
        bean.setEtime(DateTimeUtil.convertUTCToUserTime(endTime, user.getTimezone()));

        if (totalDistance == 0) {
            bean.setStartPoint("--");
            bean.setEndPoint("--");
        }

        list.add(bean);

        return list;
    }
    public List<DailySummaryReportDTO> getDailySummaryReport(
            long deviceId,
            String startTime,
            String endTime,
            Users user) {

    	boolean isAdmin = userRepository.existsByIdAndRolesRoleName(
    	        user.getId(), "ROLE_ADMIN"
    	);
    	
    	LocalDate startDateParsed = LocalDate.parse(startTime);
    	LocalDate endDateParsed = LocalDate.parse(endTime);

        List<Object[]> rows = dailySummaryReportRepository.getDailySummaryData(
        		deviceId,
                startDateParsed,
                endDateParsed,
                user.getAdminId(),
                user.getId(),
                isAdmin
        );

        List<DailySummaryReportDTO> list = new ArrayList<>();

        for (Object[] row : rows) {

            DailySummaryReportDTO bean = new DailySummaryReportDTO();

            bean.setDeviceId(((Number) row[0]).longValue());
            bean.setDevice_name((String) row[1]);

            bean.setTotal_records(((Number) row[2]).longValue());
            bean.setTotal_distance(((Number) row[3]).doubleValue());

            long totalIdle = ((Number) row[4]).longValue();
            long totalMovement = ((Number) row[5]).longValue();

            // ✅ Your DTO expects STRING → correct conversion
            bean.setTotal_idle_time(
                    DateTimeUtil.getTimeDiffInDays(totalIdle * 1000)
            );

            bean.setTotal_movement_time(
                    DateTimeUtil.getTimeDiffInDays(totalMovement * 1000)
            );

            bean.setTotal_ignition_on_time(((Number) row[6]).longValue());
            bean.setTotal_overspeed(((Number) row[7]).longValue());

            bean.setDate(row[8] != null ? row[8].toString() : "");

            bean.setMaximum_speed(((Number) row[9]).doubleValue());
            bean.setMinimum_speed(((Number) row[10]).doubleValue());
            bean.setAverage_speed(((Number) row[11]).doubleValue());
            bean.setFuel_consumption(((Number) row[12]).doubleValue());

            list.add(bean);
        }

        return list;
    }

}