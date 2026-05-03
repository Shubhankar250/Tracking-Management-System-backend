package com.trackingpath.controllers;


import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trackingpath.dtos.RouteImportResponse;
import com.trackingpath.dtos.RoutePlannerRequest;
import com.trackingpath.dtos.RoutePlannerResponse;
import com.trackingpath.services.GpsHistoryImportService;
import com.trackingpath.services.KmlImportService;
import com.trackingpath.services.TransportRoutePlannerService;

@RestController
@RequestMapping("/transport/planner")
@CrossOrigin
public class TransportRoutePlannerController {

	@Autowired
	private ObjectMapper mapper;
    private final KmlImportService kmlImportService;
    private final GpsHistoryImportService gpsHistoryImportService;
    private final TransportRoutePlannerService plannerService;

    public TransportRoutePlannerController(
            KmlImportService kmlImportService,
            GpsHistoryImportService gpsHistoryImportService,
            TransportRoutePlannerService plannerService
    ) {
        this.kmlImportService = kmlImportService;
        this.gpsHistoryImportService = gpsHistoryImportService;
        this.plannerService = plannerService;
    }

    @PostMapping(value = "/import-kml-text", consumes = MediaType.APPLICATION_JSON_VALUE)
    public RouteImportResponse importKmlText(@RequestBody Map<String, String> body) {
        String kml = body.get("kml");
        return kmlImportService.parseKml(kml);
    }

    @PostMapping(value = "/import-kml-file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public RouteImportResponse importKmlFile(@RequestParam("file") MultipartFile file) throws IOException {
        String text = new String(file.getBytes());
        return kmlImportService.parseKml(text);
    }

    @PostMapping(value = "/import-gps-file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public RouteImportResponse importGpsFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "5") int idleMinutes,
            @RequestParam(defaultValue = "50") double stopRadiusMeters
    ) throws IOException {
        return gpsHistoryImportService.importGpsCsv(file, idleMinutes, stopRadiusMeters);
    }

    @PostMapping(value = "/import-gps-text", consumes = MediaType.APPLICATION_JSON_VALUE)
    public RouteImportResponse importGpsText(@RequestBody Map<String, Object> body) throws IOException {
        String csv = (String) body.get("csv");
        int idleMinutes = body.get("idleMinutes") == null ? 5 : ((Number) body.get("idleMinutes")).intValue();
        double stopRadiusMeters = body.get("stopRadiusMeters") == null ? 50 : ((Number) body.get("stopRadiusMeters")).doubleValue();
        return gpsHistoryImportService.importGpsText(csv, idleMinutes, stopRadiusMeters);
    }

    @PostMapping(value = "/save", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public RoutePlannerResponse save(
            @RequestPart("route") String routeJson,
            @RequestPart(value = "files", required = false) List<MultipartFile> files
    ) throws Exception {
        RoutePlannerRequest request = mapper.readValue(routeJson, RoutePlannerRequest.class);

        return plannerService.save(request, files);
    }

    @PutMapping(value = "/{routeId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public RoutePlannerResponse update(
            @PathVariable Long routeId,
            @RequestPart("route") String routeJson,
            @RequestPart(value = "files", required = false) List<MultipartFile> files
    ) throws Exception {
        RoutePlannerRequest request = mapper.readValue(routeJson, RoutePlannerRequest.class);
        request.setRouteId(routeId);

        return plannerService.update(request, files);
    }
    
    
    @GetMapping("/list")
	public Page<RoutePlannerResponse> getRoutes(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "25") int size, @RequestParam(required = false) String search,
			@RequestParam(required = false) String routeType) {
		return plannerService.getRoutes(page, size, search, routeType);
	}

    @GetMapping("/{routeId}")
    @Transactional

    public RoutePlannerResponse getRoute(@PathVariable Long routeId) {
        return plannerService.getRoute(routeId);
    }
}
