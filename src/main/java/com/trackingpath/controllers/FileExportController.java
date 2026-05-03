package com.trackingpath.controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import com.trackingpath.dtos.ActivityLogDTO;
import com.trackingpath.dtos.DailySummaryReportDTO;
import com.trackingpath.dtos.DistanceReportDto;
import com.trackingpath.dtos.EventDataDTO;
import com.trackingpath.dtos.HistoryDataPlaybackDTO;
import com.trackingpath.entities.Users;
import com.trackingpath.export.ActivityLogCSV;
import com.trackingpath.export.ActivityLogExcel;
import com.trackingpath.export.HistoryCSVView;
import com.trackingpath.export.HistoryGPXView;
import com.trackingpath.export.HistoryGSRView;
import com.trackingpath.export.HistoryKMLView;
import com.trackingpath.services.ActivityLogService;
import com.trackingpath.services.AuthenticationService;
import com.trackingpath.services.DeviceService;
import com.trackingpath.services.ReportService;
import com.trackingpath.util.DateTimeUtil;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

    @RestController
	@RequestMapping("/export")
	public class FileExportController {

	    @Autowired
	    private ReportService reportService;
	    @Autowired
	    private AuthenticationService authenticationService;
	    @Autowired 
	  ActivityLogService activityLogService;
	    @Autowired 
	    DeviceService deviceService;
	    @GetMapping("/report")
	    public ModelAndView exportReport(
	            @RequestParam("reportType") String reportType,
	            @RequestParam(value = "type", required = false) String type,
	            @RequestParam(value = "deviceIds", required = false) String deviceIdsParam,
	            @RequestParam(value = "from_date", required = false) String from_date,
	            @RequestParam(value = "to_date", required = false) String to_date,
	            @RequestParam(value = "geofences", required = false) String geofenceParam,
	            @RequestParam(value = "speed", required = false) String speed,
	            @RequestParam(value = "selectedColumns", required = false) String[] selectedColumnLabels,
	            HttpServletResponse response
	    ) throws Exception {

	        Users user = authenticationService.getCurrentUser();
	        long tm = System.currentTimeMillis() / 1000;

	        // ---------- Parse deviceIds ----------
	        List<Long> deviceIds = parseIds(deviceIdsParam);

	        // ---------- Parse geofences ----------
	        List<Long> geofences = parseIds(geofenceParam);

	        // ---------- Column label → key ----------
	        List<String> selectedColumnKeys = mapSelectedColumns(selectedColumnLabels);

	        ModelAndView model = new ModelAndView();

	        // ---------- SWITCH BY REPORT TYPE ----------
	        switch (reportType.toLowerCase()) {

	            case "movementreport":
	                model.addObject("MovementReport",
	                        reportService.getMovementReport(
	                                from_date, to_date, deviceIds, geofences, speed,user));

	                model.addObject("selectedColumns", selectedColumnKeys);

	                setViewAndHeader(type, response, "MovementReport", tm, model);
	                break;
	                
	            case "overspeedreport":

	                model.addObject("OverspeedReport",
	                        reportService.getOverspeedReport(
	                                from_date,
	                                to_date,
	                                deviceIds,
	                                speed,
	                                user
	                        )
	                );

	                model.addObject("selectedColumns", selectedColumnKeys);

	                setViewAndHeader(type, response, "OverspeedReport", tm, model);

	                break;    
	                
	            case "distancereport":

	                if (deviceIds == null || deviceIds.isEmpty()) {
	                    throw new IllegalArgumentException("No device selected");
	                }

	                List<DistanceReportDto> finalList = new ArrayList<>();
	                
	                String fromDateTime = from_date + " 00:00:00";
                	String toDateTime = to_date + " 23:59:59";

	                for (Long deviceId : deviceIds) {

	                	finalList.addAll(
	                	    reportService.distanceReport(
	                	        deviceId,
	                	        fromDateTime,
	                	        toDateTime,
	                	        user
	                	    )
	                	);
	                }

	                model.addObject("DistanceReport", finalList);
	                model.addObject("selectedColumns", selectedColumnKeys);

	                setViewAndHeader(type, response, "DistanceReport", tm, model);
	                break;   
	                
	            case "dailysummaryreport":

	                if (deviceIds == null || deviceIds.isEmpty()) {
	                    throw new IllegalArgumentException("No device selected");
	                }

	                List<DailySummaryReportDTO> dailySummaryList = new ArrayList<>();

	                for (Long deviceId : deviceIds) {
	                    dailySummaryList.addAll(
	                        reportService.getDailySummaryReport(
	                            deviceId,
	                            from_date,
	                            to_date,
	                            user
	                        )
	                    );
	                }

	                model.addObject("DailySummaryReport", dailySummaryList);
	                model.addObject("selectedColumns", selectedColumnKeys);

	                setViewAndHeader(type, response, "DailySummaryReport", tm, model);
	                break;    

				/*
				 * case "parkingreport": model.addObject("ParkingReport",
				 * reportService.getParkingReport( from_date, to_date, deviceIds, user));
				 * 
				 * setViewAndHeader(type, response, "ParkingReportData", tm, model); break;
				 * 
				 * case "tripreport": model.addObject("TripReport", reportService.getTripReport(
				 * from_date, to_date, deviceIds, user));
				 * 
				 * setViewAndHeader(type, response, "TripReportData", tm, model); break;
				 */

	            default:
	                throw new IllegalArgumentException("Invalid report type: " + reportType);
	        }

	        return model;
	    }

	    // ================= HELPER METHODS =================

	    private List<Long> parseIds(String param) {
	        List<Long> ids = new ArrayList<>();
	        if (param != null && !param.isEmpty()) {
	            for (String s : param.split(",")) {
	                try {
	                    ids.add(Long.parseLong(s.trim()));
	                } catch (Exception ignored) {}
	            }
	        }
	        return ids;
	    }

	    private List<String> mapSelectedColumns(String[] labels) {

	        Map<String, String> labelToKeyMap = Map.ofEntries(

	            // Movement
	            Map.entry("Name", "name"),
	            Map.entry("Time", "time"),
	            Map.entry("Latitude", "latitude"),
	            Map.entry("Longitude", "longitude"),
	            Map.entry("Speed", "speed"),
	            Map.entry("Address", "address"),
	            Map.entry("Distance", "distance"),

	            // 🔥 Overspeed (ADD THIS)
	            Map.entry("Device", "device"),
//	            Map.entry("Speed", "speed"),
	            Map.entry("Speed Limit", "speed_limit"),
//	            Map.entry("Address", "address"),
	            Map.entry("Device Time", "time"),
	            
	            Map.entry("Start Address", "start"),
	            Map.entry("End Address", "end"),
	            Map.entry("Start Time", "stime"),
	            Map.entry("End Time", "etime"),
//	            Map.entry("Distance", "distance")
	            
	            Map.entry("Date", "date"),
	            Map.entry("Movement Time", "movement"),
	            Map.entry("Idle Time", "idle")
	        );

	        List<String> keys = new ArrayList<>();

	        if (labels != null) {
	            for (String label : labels) {
	                String key = labelToKeyMap.get(label.trim());
	                if (key != null) {
	                    keys.add(key);
	                }
	            }
	        }

	        return keys;
	    }

	    private void setViewAndHeader(
	            String type,
	            HttpServletResponse response,
	            String filePrefix,
	            long tm,
	            ModelAndView model
	    ) {

	    	 if ("XLSX".equalsIgnoreCase(type)) {
		            response.setHeader(
		                    "Content-Disposition",
		                    "attachment; filename=" + filePrefix + "_" + tm + ".xlsx");
		            model.setViewName(filePrefix + "ExcelData");

		        } else if ("CSV".equalsIgnoreCase(type)) {
		            response.setHeader(
		                    "Content-Disposition",
		                    "attachment; filename=" + filePrefix + "_" + tm + ".csv");
		            model.setViewName(filePrefix + "CSV");
		        }
		        
		        else if ("HTML".equalsIgnoreCase(type)) {
		            response.setHeader(
		                "Content-Disposition",
		                "attachment; filename=" + filePrefix + "_" + tm + ".html");
		            model.setViewName(filePrefix + "HTML");

		        } else if ("JSON".equalsIgnoreCase(type)) {
		            response.setHeader(
		                "Content-Disposition",
		                "attachment; filename=" + filePrefix + "_" + tm + ".json");
		            model.setViewName(filePrefix + "JSON");

		        } else if ("PDF".equalsIgnoreCase(type)) {
		            response.setHeader(
		                "Content-Disposition",
		                "attachment; filename=" + filePrefix + "_" + tm + ".pdf");
		            model.setViewName(filePrefix + "PDF");
		        }
		    
	    }
	    
	    @GetMapping("/activitylogs")
	    public ModelAndView exportActivityLog(
	            HttpServletRequest request,
	            HttpServletResponse response,
	            @RequestParam String from,
	            @RequestParam String to,
	            @RequestParam(required = false) String module,
	            @RequestParam(required = false) String log_type,
	            @RequestParam String format) throws Exception {

	        if (log_type != null && log_type.trim().isEmpty()) {
	            log_type = null;
	        }

	        Users user = authenticationService.getCurrentUser();
	        long tm = System.currentTimeMillis() / 1000;

	        if ("excel".equalsIgnoreCase(format)) {
	            response.setHeader("Content-Disposition", "attachment; filename=\"ActivityLogDataExcel" + tm + ".xlsx\"");
	            ModelAndView model = new ModelAndView();
	            model.addObject("ActivityLogDataEXCEL", activityLogService.getActivityLog(user, from, to, module, log_type));
	            model.addObject("date", from + " - " + to);
	            model.addObject("org_name", user.getAccountname());
	            model.setViewName("ActivityLogExcel");
	            return model;

	        } else if ("csv".equalsIgnoreCase(format)) {
	            response.setHeader("Content-Disposition", "attachment; filename=\"ActivityLogDataCsv" + tm + ".csv\"");
	            ModelAndView model = new ModelAndView();
	            model.addObject("ActivityLogDataCSV", activityLogService.getActivityLog(user, from, to, module, log_type));
	            model.addObject("date", from + " - " + to);
	            model.addObject("org_name", user.getAccountname());
	            model.setViewName("ActivityLogCSV");
	            return model;
	        }

	        return null;
	    }
	    @GetMapping("/historyReports")
	    public ResponseEntity<byte[]> exportHistoryReport(
	            @RequestParam String type,
	            @RequestParam long deviceId,
	            @RequestParam String start_time,
	            @RequestParam String end_time,
	            @RequestParam long time_interval,
	            @RequestParam(required = false,defaultValue = "IDLING") String tripType
	    ) throws Exception {

	        Users user = authenticationService.getCurrentUser();

	        String startTime = DateTimeUtil.convertUserTimeToUTC(start_time, user.getTimezone());
	        String endTime = DateTimeUtil.convertUserTimeToUTC(end_time, user.getTimezone());

	        HistoryDataPlaybackDTO reportData =
	                deviceService.getPlaybackData(deviceId, startTime, endTime, user, time_interval,tripType );

	        String deviceName = "Device";
		    List<EventDataDTO> eventData = reportData.getEventDataList();
		    if (!eventData.isEmpty() && eventData.get(0).getDetails() != null) {
		        String rawName = eventData.get(0).getDetails().getName();
		        deviceName = rawName.replaceAll("[^a-zA-Z0-9_\\-]", "_"); // Clean for filename
		    }

		    // Format date
		 // String todayDate = new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date());
		      String startDate = start_time.split(" ")[0];
		      String endDate = end_time.split(" ")[0];

		      String fileNamePrefix = deviceName + "_" + startDate + "_to_" + endDate;
	        byte[] fileBytes = null;
	        MediaType mediaType = null;

	        switch (type.toUpperCase()) {

	            case "CSV":
	                fileBytes = HistoryCSVView.generate(reportData);
	                mediaType = MediaType.parseMediaType("text/csv");
	                fileNamePrefix += ".csv";
	                break;

	            case "KML":
	                fileBytes = HistoryKMLView.generate(reportData);
	                mediaType = MediaType.parseMediaType("application/vnd.google-earth.kml+xml");
	                fileNamePrefix += ".kml";
	                break;

	            case "GPX":
	                fileBytes = HistoryGPXView.generate(reportData);
	                mediaType = MediaType.parseMediaType("application/gpx+xml");
	                fileNamePrefix += ".gpx";
	                break;

	            case "GSR":
	                fileBytes = HistoryGSRView.generate(reportData);
	                mediaType = MediaType.APPLICATION_JSON;
	                fileNamePrefix += ".gsr";
	                break;

	            default:
	                throw new RuntimeException("Invalid type");
	        }

	        return ResponseEntity.ok()
	                .header(HttpHeaders.CONTENT_DISPOSITION,
	                        "attachment; filename=\"" + fileNamePrefix + "\"")
	                .header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "Content-Disposition") // 🔥 IMPORTANT
	                .contentType(mediaType)
	                .body(fileBytes);
	    }
	

	    
	}

