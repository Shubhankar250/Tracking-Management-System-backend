package com.trackingpath.services;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import com.trackingpath.configs.SecurityConfiguration;
import com.trackingpath.dtos.ReportScheduleDto;
import com.trackingpath.dtos.ReportScheduleLogDto;
import com.trackingpath.entities.ReportSchedule;
import com.trackingpath.entities.ReportScheduleLog;
import com.trackingpath.entities.SetupTemplateEntity;
import com.trackingpath.entities.Users;
import com.trackingpath.repositories.ReportScheduleLogRepository;
import com.trackingpath.repositories.ReportScheduleRepository;
import com.trackingpath.repositories.SetupTemplateRepository;

@Service
public class ReportScheduleService {

    private final SecurityConfiguration securityConfiguration;

    @Autowired
    private ReportScheduleRepository repository;
    @Autowired
    private ReportScheduleLogRepository logrepository;
    
    @Autowired
    private SetupTemplateRepository templateRepository;

    ReportScheduleService(SecurityConfiguration securityConfiguration) {
        this.securityConfiguration = securityConfiguration;
    }

    public ReportSchedule create(ReportScheduleDto dto, Users user) {
    	
       // validate(dto);
        ReportSchedule s = new ReportSchedule();
        copy(dto, s,user);

        // ✅ FIX: logged-in user ka ID set karo
        s.setUserId(user.getId());

        return repository.save(s);
    }

    public ReportSchedule update(Long id, ReportScheduleDto dto,Users user) {
        ReportSchedule s = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Schedule not found with id: " + id));

      
        copy(dto, s,user);
        return repository.save(s);
    }

    public void deactivate(Long id) {
        ReportSchedule s = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Schedule not found with id: " + id));
        s.setActive(false);
        repository.save(s);
    }

    public ReportSchedule getActiveSchedule(Long id) {
        return repository.findByIdAndActiveTrue(id).orElse(null);
    }
    public void deactivateSchedule(Long scheduleId) {
        ReportSchedule schedule = repository.findById(scheduleId).orElse(null);
        if (schedule != null) {
            schedule.setActive(false);
            repository.save(schedule);
        }
    }
    private void validate(ReportScheduleDto dto) {
        if (dto.getScheduleType() == null || dto.getScheduleType().isBlank()) {
            throw new IllegalArgumentException("scheduleType is required");
        }

        if (dto.getReportType() == null || dto.getReportType().isBlank()) {
            throw new IllegalArgumentException("reportType is required");
        }

        if (dto.getEmailTo() == null || dto.getEmailTo().isBlank()) {
            throw new IllegalArgumentException("emailTo is required");
        }

        if ("CRON".equalsIgnoreCase(dto.getScheduleType())) {
            if (dto.getCronExpression() == null || dto.getCronExpression().isBlank()) {
                throw new IllegalArgumentException("cronExpression is required for CRON schedule");
            }
        }

        if ("ONCE".equalsIgnoreCase(dto.getScheduleType())) {
            if (dto.getOneTimeRunAt() == null) {
                throw new IllegalArgumentException("oneTimeRunAt is required for ONCE schedule");
            }
        }
    }
    private String buildCronExpression(ReportScheduleDto dto,Users user) {

        // ✅ Take user timezone (fallback to UTC if null)
        ZoneId userZone = (user.getTimezone() != null && !user.getTimezone().isEmpty())
                ? ZoneId.of(user.getTimezone())
                : ZoneId.of("UTC");
         System.out.println("timezone"+user.getTimezone());
        ZoneId utcZone = ZoneId.of("UTC");

        // ✅ DAILY → "01:01"
        if (dto.getDaily() != null && !dto.getDaily().equalsIgnoreCase("false")) {

            String[] time = dto.getDaily().split(":");

            LocalTime userTime = LocalTime.of(
                    Integer.parseInt(time[0]),
                    Integer.parseInt(time[1])
            );

            LocalDate today = LocalDate.now(userZone);

            ZonedDateTime userDateTime = ZonedDateTime.of(today, userTime, userZone);
            ZonedDateTime utcDateTime = userDateTime.withZoneSameInstant(utcZone);

            return String.format("0 %d %d * * ?",
                    utcDateTime.getMinute(),
                    utcDateTime.getHour());
        }

        // ✅ WEEKLY → "mon_01:01"
        if (dto.getWeekly() != null && !dto.getWeekly().equalsIgnoreCase("false")) {

            String[] parts = dto.getWeekly().split("_");

            String day = parts[0].toUpperCase();
            String[] time = parts[1].split(":");

            LocalTime userTime = LocalTime.of(
                    Integer.parseInt(time[0]),
                    Integer.parseInt(time[1])
            );

            LocalDate today = LocalDate.now(userZone);

            ZonedDateTime userDateTime = ZonedDateTime.of(today, userTime, userZone);
            ZonedDateTime utcDateTime = userDateTime.withZoneSameInstant(utcZone);

            return String.format("0 %d %d ? * %s",
                    utcDateTime.getMinute(),
                    utcDateTime.getHour(),
                    day);
        }

        // ✅ MONTHLY → "1_01:01"
        if (dto.getMonthly() != null && !dto.getMonthly().equalsIgnoreCase("false")) {

            String[] parts = dto.getMonthly().split("_");

            int dayOfMonth = Integer.parseInt(parts[0]);
            String[] time = parts[1].split(":");

            LocalTime userTime = LocalTime.of(
                    Integer.parseInt(time[0]),
                    Integer.parseInt(time[1])
            );

            LocalDate baseDate = LocalDate.now(userZone).withDayOfMonth(1);

            ZonedDateTime userDateTime = ZonedDateTime.of(
                    baseDate.withDayOfMonth(dayOfMonth),
                    userTime,
                    userZone
            );

            ZonedDateTime utcDateTime = userDateTime.withZoneSameInstant(utcZone);

            return String.format("0 %d %d %d * ?",
                    utcDateTime.getMinute(),
                    utcDateTime.getHour(),
                    dayOfMonth);
        }

        throw new IllegalArgumentException("No valid schedule provided");
    }
    private void copy(ReportScheduleDto dto, ReportSchedule s,Users user) {

        s.setScheduleName(dto.getReportType());
        s.setReportType(dto.getReportType());
        s.setEmailTo(dto.getEmailTo());
        s.setEmailCc(dto.getEmailCc());

        s.setOutputFormat(dto.getOutputFormat());
        s.setScheduleType(dto.getScheduleType());
        boolean hasSchedule =
                (dto.getDaily() != null && !"false".equalsIgnoreCase(dto.getDaily())) ||
                (dto.getWeekly() != null && !"false".equalsIgnoreCase(dto.getWeekly())) ||
                (dto.getMonthly() != null && !"false".equalsIgnoreCase(dto.getMonthly()));

        if (hasSchedule) {
            String cron = buildCronExpression(dto, user);
            s.setCronExpression(cron);           
        } else {
            s.setCronExpression(null);  // optional
        }
        
        s.setTimezone(user.getTimezone());
        s.setFilterJson(dto.getFilterJson());
        s.setOneTimeRunAt(dto.getOneTimeRunAt());
        s.setActive(dto.getActive() == null ? true : dto.getActive());

        // 🔥 NEW FIELDS mapping
        s.setTitle(dto.getTitle());
        s.setPeriod(dto.getPeriod());
        s.setSpeed_limit(dto.getSpeed_limit());
        s.setStops(dto.getStops());
        s.setDaily(dto.getDaily());
        s.setWeekly(dto.getWeekly());
        s.setMonthly(dto.getMonthly());
        s.setFrom_date(dto.getFrom_date());
        s.setTo_date(dto.getTo_date());
        s.setTemplate_id(dto.getTemplate_id());
        if (dto.getTemplate_id() != null) {

            SetupTemplateEntity template = templateRepository
                .findById(dto.getTemplate_id())
                .orElseThrow(() -> new RuntimeException("Template not found"));

            s.setSubject(template.getSubject());   
            s.setEmail_body(template.getMessage());    

        } else {
            // fallback (optional)
            s.setSubject(dto.getSubject());
            s.setEmail_body(dto.getEmail_body());
        }
       
        // ✅ List → String conversion
        if (dto.getDevices() != null) {
            s.setDevices(dto.getDevices().toString()); // [1,2,3]
        }

        if (dto.getGeofences() != null) {
            s.setGeofences(dto.getGeofences().toString());
        }

        if (dto.getSkip_column() != null) {
            s.setSkip_column(String.join(",", dto.getSkip_column()));
        }
    }
    public Page<ReportScheduleDto> getScheduleData(Users user, String search, Pageable pageable) {

        String searchParam = (search != null ? search.trim() : "");

        Page<ReportScheduleDto> page =
                repository.getScheduleData(user.getId(), searchParam, pageable);

        ZoneId userZone = ZoneId.of(
                user.getTimezone() != null ? user.getTimezone() : "Asia/Kolkata"
        );

        ZoneId utcZone = ZoneId.of("UTC");

        return page.map(dto -> {

            // 🔥 convert createdAt
            LocalDateTime created = dto.getCreatedAt() != null
                    ? dto.getCreatedAt().atZone(utcZone)
                            .withZoneSameInstant(userZone)
                            .toLocalDateTime()
                    : null;

            // 🔥 return NEW DTO (important)
            return new ReportScheduleDto(
                    dto.getId(),
                    dto.getTitle(),
                    dto.getReportType(),
                    dto.getOutputFormat(),
                    created,
                    dto.getActive(),
                    dto.getDaily(),
                    dto.getWeekly(),
                    dto.getMonthly()
            );
        });
    }
    public ReportSchedule getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Schedule not found with id: " + id));
    }
    public Page<ReportScheduleLogDto> getScheduleLogData(Users user, String search, Pageable pageable) {

        Page<ReportScheduleLogDto> logs =
                logrepository.getScheduleLogs(user.getId(), search, pageable);

        ZoneId userZone = ZoneId.of(
                user.getTimezone() != null ? user.getTimezone() : "Asia/Kolkata"
        );
        ZoneId utcZone = ZoneId.of("UTC");

        return logs.map(dto -> {

            ZonedDateTime started = dto.getStartedAt()
                    .atZone(utcZone)
                    .withZoneSameInstant(userZone);

            ZonedDateTime completed = dto.getCompletedAt() != null
                    ? dto.getCompletedAt().atZone(utcZone).withZoneSameInstant(userZone)
                    : null;

            // ===============================
            // ✅ REPORT TYPE LOGIC (NEW)
            // ===============================
            String scheduleType;

            if ((dto.getDaily() != null && !dto.getDaily().equalsIgnoreCase("false") && !dto.getDaily().isBlank())
                    || (dto.getWeekly() != null && !dto.getWeekly().equalsIgnoreCase("false") && !dto.getWeekly().isBlank())
                    || (dto.getMonthly() != null && !dto.getMonthly().equalsIgnoreCase("false") && !dto.getMonthly().isBlank())) {

                if (dto.getDaily() != null && !dto.getDaily().equalsIgnoreCase("false") && !dto.getDaily().isBlank()) {
                    scheduleType = "DAILY";
                } else if (dto.getWeekly() != null && !dto.getWeekly().equalsIgnoreCase("false") && !dto.getWeekly().isBlank()) {
                    scheduleType = "WEEKLY";
                } else if (dto.getMonthly() != null && !dto.getMonthly().equalsIgnoreCase("false") && !dto.getMonthly().isBlank()) {
                    scheduleType = "MONTHLY";
                } else {
                    scheduleType = "GENERATED";
                }

            } else {
                scheduleType = "GENERATED";
            }
         dto.setSheduleReportType(scheduleType);
            return new ReportScheduleLogDto(
                    dto.getLogId(),
                    dto.getScheduleId(),
                    dto.getTitle(),
                    dto.getReportType(),
                    dto.getOutputFormat(),
                    started.toLocalDateTime(),
                    completed != null ? completed.toLocalDateTime() : null,
                    dto.getStatus(),
                    dto.getRecipientCount(),
                    dto.getFilePath(),
                    dto.getErrorMessage(),
                    dto.getFileSize(),
                    dto.getDaily(),
                    dto.getWeekly(),
                    dto.getMonthly(),
                    dto.getSheduleReportType()
            );
        });
    }
    public void deleteScheduleLog(Long id) {
    	logrepository.deleteById(id);
    }
    public List<Map<String, Object>> getTemplates(Long userId) {

        List<Object[]> results = templateRepository.findAllTemplateByUserId(userId);

        List<Map<String, Object>> response = new ArrayList<>();

        for (Object[] row : results) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", row[0]);
            map.put("templateName", row[1]);
            response.add(map);
        }

        return response;
    }
    
}
