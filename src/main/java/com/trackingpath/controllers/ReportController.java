package com.trackingpath.controllers;

import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.trackingpath.dtos.ReportDTO;
import com.trackingpath.entities.ReportLogEntity;
import com.trackingpath.entities.Users;
import com.trackingpath.services.ActivityLogService;
import com.trackingpath.services.AuthenticationService;
import com.trackingpath.services.ReportService;

import jakarta.servlet.http.HttpServletRequest;

@RequestMapping("/reports")
@RestController
@RequiredArgsConstructor
public class ReportController {
	@Value("${REPORT_TYPES}")
	private String report_types;

    private final ReportService reportService;
    private final AuthenticationService authenticationService;
    
    @Autowired
    private ActivityLogService activityLogService;

    @PostMapping("/add")
    public ResponseEntity<?> insertReportDetails(@RequestBody ReportDTO reportBean,HttpServletRequest request) {

        Users user = authenticationService.getCurrentUser();

        boolean saved = reportService.insertReportDetails(reportBean, user);

        if (saved) {
        	activityLogService.createActivity("NEW REPORT CREATED",
					"REPORT(" + reportBean.getType() + ") CREATED BY " + user.getUsername(), user, request);
            return ResponseEntity.ok(
                java.util.Map.of(
                    "status", "success",
                    "message", "Report created successfully!"
                )
            );
        }

        return ResponseEntity.status(400).body(
            java.util.Map.of(
                "status", "error",
                "message", "Failed to insert report"
            )
        );
    }
    @GetMapping("/getReportData")
    public ResponseEntity<Page<ReportDTO>> getReportData(
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int size,
            @RequestParam(required = false, defaultValue = "") String search
            
    ) {
        Users user = authenticationService.getCurrentUser();
        Pageable pageable = PageRequest.of(page, size);

        return ResponseEntity.ok(reportService.getReportData(user, search, pageable));
    }
    @GetMapping("/log")
    public ResponseEntity<Page<ReportLogEntity>> getReportDataLog(
    		@RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int size,
            @RequestParam(required = false, defaultValue = "") String search
    ) {
    	Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(reportService.getReportDataLog(search, pageable));
    }
    @DeleteMapping("/delete")
    public ResponseEntity<Map<String, String>> deleteReport(@RequestParam("id") Long id,HttpServletRequest request) {

        Users user = authenticationService.getCurrentUser();    
        boolean deleted = reportService.deleteReport(id, user);

        if (deleted) {
        	activityLogService.createActivity("REPORT DELETED",
					"REPORT DELETED WITH ID (" + id + ") DELETED BY " + user.getUsername(), user, request);
            return ResponseEntity.ok(
                    Map.of("status", "success", "message", "Report deleted successfully")
            );
        }

        return ResponseEntity.status(404).body(
                Map.of("status", "error", "message", "Report not found")
        );
    }

    @DeleteMapping("/log/delete")
    public ResponseEntity<Map<String, String>> deleteReportLog(@RequestParam("id") Long id,HttpServletRequest request) {

        Users user = authenticationService.getCurrentUser();
        boolean deleted = reportService.deleteReportLog(id, user);

        if (deleted) {
        	activityLogService.createActivity("REPORT LOG DELETED",
					"REPORT LOG DELETED WITH ID (" + id + ") DELETED BY " + user.getUsername(), user, request);
            return ResponseEntity.ok(
                    Map.of("status", "success", "message", "Report Log deleted successfully")
            );
        }

        return ResponseEntity.status(404).body(
                Map.of("status", "error", "message", "Report Log not found")
        );
    }
    @GetMapping("/byId")
    public ResponseEntity<ReportDTO> getReportById(@RequestParam Long id) {
        return ResponseEntity.ok(reportService.getReportById(id));
    } 
    @PostMapping("/update")
    public ResponseEntity<String> updateReportData(@RequestBody ReportDTO dto,HttpServletRequest request) {
        Users user = authenticationService.getCurrentUser();
        if (user == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        boolean updated = reportService.updateReportData(dto, user);
        activityLogService.createActivity("REPORT UPDATED",
				"REPORT(" + dto.getType() + ") UPDATED BY " + user.getUsername(), user, request);	
        return updated
                ? ResponseEntity.ok("Updated successfully!")
                : ResponseEntity.badRequest().body("Update failed or report not found");
    }
    @GetMapping("/ColumnByReportType")
    public List<String> getColumnByReportType(
            @RequestParam(name = "reportType", defaultValue = "") String reportType) {

    	Users user = authenticationService.getCurrentUser();
        if (user != null) {
            return reportService.getColumnByReportType(reportType, user);
        }
        return List.of(); // never return null
    }
    @GetMapping("/types")
	public ResponseEntity<Map<String, Object>> getMapSettings() {
    	List<String> reportTypes = Arrays.asList(report_types.split(","));
		Map<String, Object> response = new HashMap<>();
		response.put("reporttypes", reportTypes);
		return ResponseEntity.ok(response);
	}
}