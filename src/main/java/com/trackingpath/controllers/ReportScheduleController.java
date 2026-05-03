package com.trackingpath.controllers;

import com.trackingpath.dtos.ReportScheduleDto;
import com.trackingpath.dtos.ReportScheduleLogDto;
import com.trackingpath.entities.ReportSchedule;
import com.trackingpath.entities.SetupTemplateEntity;
import com.trackingpath.entities.Users;
import com.trackingpath.scheduler.QuartzReportSchedulerService;
import com.trackingpath.services.AuthenticationService;
import com.trackingpath.services.ReportExecutionService;
import com.trackingpath.services.ReportScheduleService;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/report-schedules")
public class ReportScheduleController {

    @Autowired
    private ReportScheduleService reportScheduleService;

    @Autowired
    private QuartzReportSchedulerService quartzSchedulerService;

    @Autowired
    private ReportExecutionService reportExecutionService;
    @Autowired
     AuthenticationService authenticationService;
    @PostMapping
    public ResponseEntity<?> create(@RequestBody ReportScheduleDto dto) throws Exception {
    	  Users user = authenticationService.getCurrentUser();
        ReportSchedule schedule = reportScheduleService.create(dto,user);
        quartzSchedulerService.scheduleJob(schedule);
        return ResponseEntity.ok(schedule);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody ReportScheduleDto dto) throws Exception {
    	 Users user = authenticationService.getCurrentUser();
        ReportSchedule schedule = reportScheduleService.update(id, dto,user);
        quartzSchedulerService.scheduleJob(schedule);
        return ResponseEntity.ok(schedule);
    }
    @GetMapping("/{id}")
    public ResponseEntity<ReportScheduleDto> getById(@PathVariable Long id) {
        ReportSchedule schedule = reportScheduleService.getById(id);
        return ResponseEntity.ok(convertToDto(schedule));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) throws Exception {
        reportScheduleService.deactivate(id);
        quartzSchedulerService.deleteJob(id);
        return ResponseEntity.ok("Schedule deleted successfully");
    }
    @GetMapping("/data")
    public ResponseEntity<Page<ReportScheduleDto>> getScheduleData(
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int size,
            @RequestParam(required = false, defaultValue = "") String search
    ) {
        Users user = authenticationService.getCurrentUser();
        Pageable pageable = PageRequest.of(page, size);

        return ResponseEntity.ok(reportScheduleService.getScheduleData(user, search, pageable));
    }

    @PostMapping("/{id}/run-now")
    public ResponseEntity<?> runNow(@PathVariable Long id) {
        ReportSchedule schedule = reportScheduleService.getActiveSchedule(id);
        if (schedule == null) {
            return ResponseEntity.badRequest().body("Active schedule not found");
        }

        reportExecutionService.executeSchedule(schedule);
        return ResponseEntity.ok("Report executed successfully");
    }
    private ReportScheduleDto convertToDto(ReportSchedule s) {
        ReportScheduleDto dto = new ReportScheduleDto();

        dto.setId(s.getId());
        dto.setTitle(s.getTitle());
        dto.setReportType(s.getReportType());
        dto.setEmailTo(s.getEmailTo());
        dto.setEmailCc(s.getEmailCc());
        dto.setOutputFormat(s.getOutputFormat());
        dto.setPeriod(s.getPeriod());
        dto.setSpeed_limit(s.getSpeed_limit());
        dto.setStops(s.getStops());

        dto.setFrom_date(s.getFrom_date());
        dto.setTo_date(s.getTo_date());

        dto.setDaily(s.getDaily());
        dto.setWeekly(s.getWeekly());
        dto.setMonthly(s.getMonthly());

        // 🔥 reverse conversion (VERY IMPORTANT)
        if (s.getDevices() != null) {
            dto.setDevices(
                Arrays.stream(s.getDevices().replaceAll("[\\[\\] ]", "").split(","))
                      .filter(x -> !x.isEmpty())
                      .map(Long::parseLong) 
                      .toList()
            );
        }

        if (s.getGeofences() != null) {
            dto.setGeofences(
                Arrays.stream(s.getGeofences().replaceAll("[\\[\\] ]", "").split(","))
                      .filter(x -> !x.isEmpty())
                      .map(Long::parseLong) 
                      .toList()
            );
        }

        if (s.getSkip_column() != null) {
            dto.setSkip_column(Arrays.asList(s.getSkip_column().split(",")));
        }

        return dto;
    }
    @GetMapping("/log")
    public ResponseEntity<Page<ReportScheduleLogDto>> getScheduleLogData(
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int size,
            @RequestParam(required = false, defaultValue = "") String search
    ) {
        Users user = authenticationService.getCurrentUser();
        Pageable pageable = PageRequest.of(page, size);

        return ResponseEntity.ok(reportScheduleService.getScheduleLogData(user, search, pageable));
    }
    @DeleteMapping("/log/{id}")
    public ResponseEntity<?> deleteScheduleLog(@PathVariable Long id) {
        reportScheduleService.deleteScheduleLog(id);
        return ResponseEntity.ok("Log deleted successfully");
    }
    @GetMapping("/template")
    public ResponseEntity<List<Map<String, Object>>> getTemplates() {
        Users user = authenticationService.getCurrentUser();
        return ResponseEntity.ok(reportScheduleService.getTemplates(user.getId()));
    }

   
}
