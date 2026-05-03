package com.trackingpath.controllers;

import java.io.Console;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.trackingpath.dtos.CommandDTO;
import com.trackingpath.dtos.SnapshotResponseDto;
import com.trackingpath.entities.DeviceCommandLog;
import com.trackingpath.entities.Users;
import com.trackingpath.services.AuthenticationService;
import com.trackingpath.services.CommandService;
import com.trackingpath.util.Jt808AllCmdHexUtil;
import com.trackingpath.util.Jt808SnapshotBuilder;

@RestController
@RequestMapping("/command")
public class SendCommandController {
	
	private static final Logger log = LoggerFactory.getLogger(SendCommandController.class);
	@Value("${cmd.server.url}")
	private String COMMAND_SERVER_URL;
	
	@Value("${FRP_ACCESS_HOST}")
	private String FRP_ACCESS_HOST;
	@Value("${FRP_HOST}")
	private String FRP_HOST;
	@Value("${FRP_PORT}")
	private String FRP_PORT;
	@Value("${FRP_TOKEN}")
	private String FRP_TOKEN;
	@Autowired
	private CommandService commandService;
	@Autowired
	AuthenticationService authenticationService;
	@PostMapping("/snapshot")
	  public ResponseEntity<?> takeSnapshot(
	          @RequestParam String sim,
	          @RequestParam int deviceId,
	          @RequestParam(defaultValue = "1") int channel	         
	  ) {

	      if (sim == null || !sim.matches("\\d{10,20}")) {
	          return ResponseEntity.badRequest()
	                  .body(Map.of("error", "SIM must be digits (10-20)"));
	      }

	      if (channel < 1 || channel > 16) {
	          return ResponseEntity.badRequest()
	                  .body(Map.of("error", "channel must be 1-16"));
	      }	     
	      try {	       
	          String hex = Jt808SnapshotBuilder.build8801TakePhoto(
	        		   sim,

	        			2,

	        			channel
	        			);           // quality	      
	          System.out.println(hex);

	          String result = sendCommands(deviceId, hex);
	  		  Users user = authenticationService.getCurrentUser();

	         CommandDTO commandBean = new CommandDTO();
	         commandBean.setDeviceId(deviceId);
	         commandBean.setCommandName("SNAPSHOT");
	         commandBean.setCommandMsg("SUCCESS");
	         commandBean.setCommandCategory("QUERY");
	         commandBean.setCommandSubCategory("SNAPSHOT");

	         DeviceCommandLog savedLog = commandService.insertCommandLogWithChannel(commandBean, user, "send successfully", channel);

	          Map<String,Object> resp = new LinkedHashMap<>();
	          resp.put("success", true);
	          resp.put("hex8801", hex);
	          resp.put("deviceId", deviceId);
	          resp.put("channel", channel);	         
	          resp.put("cmdResult", result);
	          resp.put("commandid", savedLog.getId());
	          return ResponseEntity.ok(resp);

	      } catch (Exception e) {

	          return ResponseEntity.status(500)
	                  .body(Map.of(
	                          "success", false,
	                          "error", "Failed to send snapshot command",
	                          "details", e.getMessage()
	                  ));
	      }
	  }
	 @PostMapping("/snapshot-ack")
	  public ResponseEntity<?> snapshotAck(@RequestBody Map<String,Object> body){

	      log.info("Snapshot ACK: {}", body);

	      return ResponseEntity.ok(Map.of("ok", true));
	  }
	
	/* ----------------------- Command Sender ----------------------- */

	public String sendCommands(int deviceId, String command) {
		log.info("hex cmd -> {}", command);

		JSONObject attrs = new JSONObject().put("data", command);
		JSONObject payload = new JSONObject().put("type", "custom").put("deviceId", deviceId).put("attributes", attrs);

		RestTemplate rt = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		ResponseEntity<String> resp = rt.postForEntity(COMMAND_SERVER_URL,
				new HttpEntity<>(payload.toString(), headers), String.class);

		String body = resp.getBody() == null ? "{}" : resp.getBody();
		JSONObject response = new JSONObject(body);
		log.info("command server resp -> {}", response);

		return resp.getStatusCode().is2xxSuccessful() ? "success" : "Failed";
	}
	
	
	@PostMapping("/remote-access")
	public ResponseEntity<?> sendDashcamRemoteAccess(
	        @RequestParam String sim,
	        @RequestParam int deviceId
	) {

        String text = "#FRPSET:" + FRP_HOST + "," + FRP_PORT + "," + FRP_TOKEN;

	    if (sim == null || !sim.matches("\\d{10,20}")) {
	        return ResponseEntity.badRequest()
	                .body(Map.of("error", "SIM must be digits (10-20)"));
	    }

	    try {

	        // ✅ FIXED SERIAL = 1
	        int serial = 1;

	        // 🔥 build 8300 hex
	        String hex8300 = Jt808AllCmdHexUtil.build8300(
	                sim,
	                serial,
	                text
	        );

	        String result = sendCommands(deviceId, hex8300);
	        String accessUrl = "http://" + sim + "." + FRP_ACCESS_HOST + "/index.html?lang=en";

	        Map<String, Object> resp = new LinkedHashMap<>();
	        resp.put("success", true);
	        resp.put("url",accessUrl);
	        resp.put("cmdResult", result);

	        return ResponseEntity.ok(resp);

	    } catch (Exception e) {

	        return ResponseEntity.status(500)
	                .body(Map.of(
	                        "success", false,
	                        "error", "Failed to send text command",
	                        "details", e.getMessage()
	                ));
	    }
	}
	@PostMapping("/terminal-configuration")
	  public ResponseEntity<?> terminalConfiguration(
	          @RequestParam String sim,
	          @RequestParam int deviceId	          
	  ) {

	      if (sim == null || !sim.matches("\\d{10,20}")) {
	          return ResponseEntity.badRequest()
	                  .body(Map.of("error", "SIM must be digits (10-20)"));
	      }
	      try {	       
	          String hex = Jt808SnapshotBuilder.build8104(
	        		  sim,
                       1
	        		);          
	          System.out.println(hex);

	          String result = sendCommands(deviceId, hex);
	          Users user = authenticationService.getCurrentUser();

		         CommandDTO commandBean = new CommandDTO();
		         commandBean.setDeviceId(deviceId);
		         commandBean.setCommandName("TERMINAL_CONFIGURATION");
		         commandBean.setCommandMsg("SUCCESS");
		         commandBean.setCommandCategory("CONFIGURATION");
		         commandBean.setCommandSubCategory("TERMINAL_CONFIGURATION");

		         DeviceCommandLog savedLog = commandService.insertCommandLog(commandBean, user, "send successfully");
	          Map<String,Object> resp = new LinkedHashMap<>();
	          resp.put("success", true);
	          resp.put("hex8104", hex);
	          resp.put("deviceId", deviceId);	         
	          resp.put("cmdResult", result);
	          resp.put("commandid", savedLog.getId());

	          return ResponseEntity.ok(resp);

	      } catch (Exception e) {

	          return ResponseEntity.status(500)
	                  .body(Map.of(
	                          "success", false,
	                          "error", "Failed to send terminal configuration command",
	                          "details", e.getMessage()
	                  ));
	      }
	  }
	@GetMapping("/snapshot-data")
	public ResponseEntity<?> getSnapshotData(
	        @RequestParam int deviceId,
	        @RequestParam String startTime,
	        @RequestParam String endTime,
	        @RequestParam(required = false) Integer channel 
	) {

	    try {
	        List<Map<String, Object>> data =
	                commandService.getSnapshotResponse(deviceId, startTime, endTime, channel);

	        return ResponseEntity.ok(Map.of(
	                "success", true,
	                "data", data
	        ));

	    } catch (Exception e) {
	        return ResponseEntity.status(500).body(Map.of(
	                "success", false,
	                "error", e.getMessage()
	        ));
	    }
	}
	@PostMapping("/restart-command")
	  public ResponseEntity<?> restartCommand(
	          @RequestParam String sim,
	          @RequestParam int deviceId	          
	  ) {

	      if (sim == null || !sim.matches("\\d{10,20}")) {
	          return ResponseEntity.badRequest()
	                  .body(Map.of("error", "SIM must be digits (10-20)"));
	      }
	      try {	       
	          String hex = Jt808SnapshotBuilder.build8105Restart(
	        		  sim,
                   3
	        		);          
	          System.out.println(hex);

	          String result = sendCommands(deviceId, hex);
	          Users user = authenticationService.getCurrentUser();

		         CommandDTO commandBean = new CommandDTO();
		         commandBean.setDeviceId(deviceId);
		         commandBean.setCommandName("RESTART_COMMAND");
		         commandBean.setCommandMsg("SUCCESS");
		         commandBean.setCommandCategory("RESTART");
		         commandBean.setCommandSubCategory("RESTART_COMMAND");

		         DeviceCommandLog savedLog = commandService.insertCommandLog(commandBean, user, "send successfully");
	          Map<String,Object> resp = new LinkedHashMap<>();
	          resp.put("success", true);
	          resp.put("hex8104", hex);
	          resp.put("deviceId", deviceId);	         
	          resp.put("cmdResult", result);
	          resp.put("commandid", savedLog.getId());

	          return ResponseEntity.ok(resp);

	      } catch (Exception e) {

	          return ResponseEntity.status(500)
	                  .body(Map.of(
	                          "success", false,
	                          "error", "Failed to send restart command",
	                          "details", e.getMessage()
	                  ));
	      }
	  }
	@GetMapping("/terminal-configuration-data")
	public ResponseEntity<?> getTerminalConfigurationData(@RequestParam int deviceId) {

	    try {
	        List<Map<String, Object>> data =
	                commandService.getTerminalConfigurationResponse(deviceId);

	        return ResponseEntity.ok(Map.of(
	                "success", true,
	                "data", data
	        ));

	    } catch (Exception e) {
	        return ResponseEntity.status(500).body(Map.of(
	                "success", false,
	                "error", e.getMessage()
	        ));
	    }
	}
}
