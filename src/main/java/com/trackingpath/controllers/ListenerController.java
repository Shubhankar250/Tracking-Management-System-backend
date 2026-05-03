package com.trackingpath.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.trackingpath.dtos.SnapshotFileDto;
import com.trackingpath.dtos.SnapshotResponseDto;
import com.trackingpath.dtos.TerminalParamsDto;
import com.trackingpath.services.CommandService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/hook")
@RequiredArgsConstructor
public class ListenerController {

	@Autowired
	private CommandService commandService;

	@PostMapping("/snapshot-response")
	public ResponseEntity<String> receiveSnapshotResponse(@RequestBody SnapshotResponseDto dto) {

		commandService.updateSnapshotResponse(dto);

		return ResponseEntity.ok("Saved successfully");
	}

	@PostMapping("/snapshot-file")
	public ResponseEntity<String> snapshotFile(@RequestBody SnapshotFileDto dto) {

		commandService.saveSnapshotFile(dto);

		return ResponseEntity.ok("Snapshot file response saved");
	}
	 @PostMapping("/terminal-params")
	    public ResponseEntity<String> receiveTerminalParams(
	            @RequestBody TerminalParamsDto dto) {

		  commandService.saveTerminalParams(dto);

	        return ResponseEntity.ok("Terminal params saved");
	    }
}
