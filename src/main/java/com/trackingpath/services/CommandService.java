package com.trackingpath.services;

import java.io.IOException;
import java.lang.System.Logger;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trackingpath.dtos.CommandDTO;
import com.trackingpath.dtos.SnapshotFileDto;
import com.trackingpath.dtos.SnapshotResponseDto;
import com.trackingpath.dtos.TerminalParamsDto;
import com.trackingpath.entities.CommandEntity;
import com.trackingpath.entities.DeviceCommandLog;
import com.trackingpath.entities.Users;
import com.trackingpath.repositories.CommandRepository;
import com.trackingpath.repositories.DeviceCommandLogRepository;
import com.trackingpath.repositories.DeviceRepository;
import com.trackingpath.util.DateTimeHelper;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONObject;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import java.io.ByteArrayOutputStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommandService {

	private final CommandRepository commandRepository;
	private final DeviceCommandLogRepository deviceCommandLogRepository;
	private final DeviceRepository deviceRepository;

	public boolean addCommand(CommandDTO dto) {
		// System.out.println(dto);
		// Convert DTO → Entity
		CommandEntity entity = CommandEntity.builder().model(dto.getModel()).commandName(dto.getCommandName())
				.commandCode(dto.getCommandCode()).commandStatus(dto.getCommandStatus()).types(dto.getTypes())
				.commandCategory(dto.getCommandCategory()) // ✅ NEW
				.commandSubCategory(dto.getCommandSubCategory()) // ✅ NEW
				.build();

		return commandRepository.save(entity).getId() != null;
	}

	public Page<CommandDTO> getAllCommands(String search, Pageable pageable) {

		Page<CommandEntity> page = commandRepository.findAllCommands(search, pageable);

		return page.map(this::convertToDTO);
	}

	public CommandDTO getCommandById(Long id) {
		return commandRepository.findById(id).map(this::convertToDTO).orElse(null);
	}

	private CommandDTO convertToDTO(CommandEntity entity) {

		CommandDTO dto = new CommandDTO();
		dto.setId(entity.getId());
		dto.setModel(entity.getModel());
		dto.setCommandName(entity.getCommandName());
		dto.setCommandCode(entity.getCommandCode());
		dto.setCommandStatus(entity.getCommandStatus());
		dto.setTypes(entity.getTypes());
		dto.setCommandCategory(entity.getCommandCategory());
		dto.setCommandSubCategory(entity.getCommandSubCategory());
		return dto;
	}

	public boolean updateCommand(CommandDTO dto) {

		Optional<CommandEntity> optional = commandRepository.findById(dto.getId());

		if (optional.isEmpty()) {
			return false;
		}

		CommandEntity entity = optional.get();

		if (dto.getModel() != null && !dto.getModel().isEmpty())
			entity.setModel(dto.getModel());

		if (dto.getCommandName() != null && !dto.getCommandName().isEmpty())
			entity.setCommandName(dto.getCommandName());

		if (dto.getCommandCode() != null && !dto.getCommandCode().isEmpty())
			entity.setCommandCode(dto.getCommandCode());

		if (dto.getCommandStatus() != null)
			entity.setCommandStatus(dto.getCommandStatus());

		if (dto.getTypes() != null && !dto.getTypes().isEmpty())
			entity.setTypes(dto.getTypes());

		// ✅ NEW FIELDS
		if (dto.getCommandCategory() != null && !dto.getCommandCategory().isEmpty())
			entity.setCommandCategory(dto.getCommandCategory());

		if (dto.getCommandSubCategory() != null && !dto.getCommandSubCategory().isEmpty())
			entity.setCommandSubCategory(dto.getCommandSubCategory());

		commandRepository.save(entity);

		return true;
	}

	public boolean deleteCommand(Long id) {

		if (!commandRepository.existsById(id)) {
			return false; // record not found
		}

		commandRepository.deleteById(id); // JPA deletes automatically

		return true;
	}

	public byte[] downloadTemplate() throws IOException {

		Workbook workbook = new XSSFWorkbook();
		Sheet sheet = workbook.createSheet("Commands");

		// Create a bold + center style
		CellStyle headerStyle = workbook.createCellStyle();
		Font font = workbook.createFont();
		font.setBold(true);
		headerStyle.setFont(font);
		headerStyle.setAlignment(HorizontalAlignment.CENTER);
		headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);

		Row header = sheet.createRow(0);

		String[] columns = { "Model", "Command_Name", "Command_Code", "Command_Status", "Types", "Command_Category", // ✅
																														// NEW
				"Command_Sub_Category" };

		for (int i = 0; i < columns.length; i++) {
			Cell cell = header.createCell(i);
			cell.setCellValue(columns[i]);
			cell.setCellStyle(headerStyle);
			sheet.autoSizeColumn(i);
		}

		// Write to byte array instead of response stream
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		workbook.write(outputStream);
		workbook.close();

		return outputStream.toByteArray();
	}

	public List<CommandDTO> parseExcel(MultipartFile file) throws IOException {
		List<CommandDTO> commandList = new ArrayList<>();

		Workbook workbook = new XSSFWorkbook(file.getInputStream());
		Sheet sheet = workbook.getSheetAt(0);

		for (int i = 1; i <= sheet.getLastRowNum(); i++) { // skip header row
			Row row = sheet.getRow(i);
			if (row != null) {
				CommandDTO dto = new CommandDTO();
				dto.setModel(getCellValueAsString(row.getCell(0)));
				dto.setCommandName(getCellValueAsString(row.getCell(1)));
				dto.setCommandCode(getCellValueAsString(row.getCell(2)));

				String statusStr = getCellValueAsString(row.getCell(3));
				dto.setCommandStatus(statusStr.isEmpty() ? null : Short.valueOf(statusStr));

				dto.setTypes(getCellValueAsString(row.getCell(4)));
				dto.setCommandCategory(getCellValueAsString(row.getCell(5))); // ✅
				dto.setCommandSubCategory(getCellValueAsString(row.getCell(6)));
				commandList.add(dto);
			}
		}

		workbook.close();
		return commandList;
	}

	// Helper method to read cell as String
	private String getCellValueAsString(org.apache.poi.ss.usermodel.Cell cell) {
		if (cell == null)
			return "";
		switch (cell.getCellType()) {
		case STRING:
			return cell.getStringCellValue().trim();
		case NUMERIC:
			return String.valueOf((int) cell.getNumericCellValue()); // convert numeric to string
		case BOOLEAN:
			return String.valueOf(cell.getBooleanCellValue());
		default:
			return "";
		}
	}

	public List<CommandDTO> parseAndSaveExcel(MultipartFile file) throws IOException {
		List<CommandDTO> savedList = new ArrayList<>();

		Workbook workbook = new XSSFWorkbook(file.getInputStream());
		Sheet sheet = workbook.getSheetAt(0);

		for (int i = 1; i <= sheet.getLastRowNum(); i++) { // skip header row
			Row row = sheet.getRow(i);
			if (row != null) {
				// Read Excel cells
				String model = getCellValueAsString(row.getCell(0));
				String commandName = getCellValueAsString(row.getCell(1));
				String commandCode = getCellValueAsString(row.getCell(2));
				String statusStr = getCellValueAsString(row.getCell(3));
				Short commandStatus = statusStr.isEmpty() ? null : Short.valueOf(statusStr);
				String types = getCellValueAsString(row.getCell(4));
				String commandCategory = getCellValueAsString(row.getCell(5)); // ✅
				String commandSubCategory = getCellValueAsString(row.getCell(6));
				// Build entity
				CommandEntity entity = CommandEntity.builder().model(model).commandName(commandName)
						.commandCode(commandCode).commandStatus(commandStatus).types(types)
						.commandCategory(commandCategory).commandSubCategory(commandSubCategory).build();
				// Save to DB
				CommandEntity saved = commandRepository.save(entity);

				// Map entity to DTO for frontend response
				CommandDTO dto = new CommandDTO();
				dto.setModel(saved.getModel());
				dto.setCommandName(saved.getCommandName());
				dto.setCommandCode(saved.getCommandCode());
				dto.setCommandStatus(saved.getCommandStatus());
				dto.setTypes(saved.getTypes());
				dto.setCommandCategory(saved.getCommandCategory());
				dto.setCommandSubCategory(saved.getCommandSubCategory());
				savedList.add(dto);
			}
		}
		workbook.close();
		return savedList;
	}

	public List<String> getDeviceModels(Users user) { // or UserBean if you prefer
		try {
			return commandRepository.findDistinctModels();
		} catch (Exception e) {
			log.error("Error fetching device models: {}", e.getMessage());
			return Collections.emptyList();
		}
	}

	public Map<String, String> getCommands(long deviceId, Users user) {

		List<Object[]> result;

		if (user.getRoles().stream().anyMatch(r -> "ROLE_ADMIN".equalsIgnoreCase(r.getRoleName()))) {
			result = commandRepository.findCommandsByDeviceId(deviceId);
		} else {
			result = commandRepository.findActiveCommandsByDeviceId(deviceId);
		}

		Map<String, String> response = new LinkedHashMap<>();
		for (Object[] row : result) {
			response.put((String) row[1], (String) row[0]); // name → code
		}
		return response;
	}

	public String getStatus(Long deviceId) {
		try {
			return deviceRepository.findStatusByDeviceId(deviceId);
		} catch (Exception e) {
			return null;
		}
	}

	public DeviceCommandLog insertCommandLog(CommandDTO commandBean, Users user, String msg) {

		DeviceCommandLog log = DeviceCommandLog.builder().deviceId(commandBean.getDeviceId())
				.commandName(commandBean.getCommandName()).commandMsg(msg).createdOn(LocalDateTime.now())
				.userId(user.getId()).adminId(user.getAdminId()).commandCategory(commandBean.getCommandCategory()) // ✅
																													// ADD
				.commandSubCategory(commandBean.getCommandSubCategory()) // ✅ ADD
				.build();

		return deviceCommandLogRepository.save(log);

	}
	public DeviceCommandLog insertCommandLogWithChannel(CommandDTO commandBean, Users user, String msg, int channelId) {

		DeviceCommandLog log = DeviceCommandLog.builder().deviceId(commandBean.getDeviceId())
				.commandName(commandBean.getCommandName()).commandMsg(msg).createdOn(LocalDateTime.now())
				.userId(user.getId()).adminId(user.getAdminId()).commandCategory(commandBean.getCommandCategory()) // ✅
																													// ADD
				.commandSubCategory(commandBean.getCommandSubCategory()) // ✅ ADD
				.channelId(channelId)
				.build();

		return deviceCommandLogRepository.save(log);

	}

	public Page<CommandDTO> getCommandLogData(Users user, String search, Pageable pageable) {
		boolean isAdmin = user.getRoles().stream().anyMatch(r -> "ROLE_ADMIN".equalsIgnoreCase(r.getRoleName()));

		Page<CommandDTO> page = deviceCommandLogRepository.findCommandLogs(isAdmin, user.getAdminId(), user.getId(),
				search, pageable);

		return page.map(log -> {

			String timezone = (log.getDeviceTimezone() == null || log.getDeviceTimezone().isBlank())
					? user.getTimezone()
					: log.getDeviceTimezone();

			LocalDateTime utcDateTime = LocalDateTime.parse(log.getCreated_on());

			LocalDateTime converted = DateTimeHelper.utcToZone(utcDateTime, timezone);

			String formattedDate = converted.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

			log.setCreated_on(formattedDate);

			return log;
		});
	}

	private final ObjectMapper objectMapper;

	@Transactional
	public void updateSnapshotResponse(SnapshotResponseDto dto) {
		try {
			String json = objectMapper.writeValueAsString(dto);
			log.info(dto.getDeviceId() + "json--" + json);
			deviceCommandLogRepository.updateLatestResponse(dto.getDeviceId(), json);

		} catch (Exception e) {
			throw new RuntimeException("Error saving snapshot response", e);
		}
	}

	@Transactional
	public void saveSnapshotFile(SnapshotFileDto dto) {
		try {
			String json = objectMapper.writeValueAsString(dto);

			deviceCommandLogRepository.updateLatestResponseByDeviceAndChannel(dto.getDeviceId(), dto.getChannel(), json);

		} catch (Exception e) {
			throw new RuntimeException("Error saving snapshot file response", e);
		}
	}

	public List<Map<String, Object>> getSnapshotResponse(
	        int deviceId,
	        String start,
	        String end,
	        Integer channel   // 👈 new param
	) {

	    String startDateTime = start + " 00:00:00";
	    String endDateTime = end + " 23:59:59";

	    List<DeviceCommandLog> logs  = deviceCommandLogRepository.getSnapshotDataWithChannel(
	                deviceId, startDateTime, endDateTime, channel);
	     
	 
	    

	    List<Map<String, Object>> result = new ArrayList<>();

	    for (DeviceCommandLog log : logs) {
	        Map<String, Object> map = new HashMap<>();

	        map.put("deviceId", log.getDeviceId());
	        map.put("createdOn", log.getCreatedOn());

	        try {
	            String responseStr = log.getResponse();

	            if (responseStr != null && responseStr.startsWith("{")) {
	                JSONObject json = new JSONObject(responseStr);

	                String filePath = json.optString("file");
	                Integer ch = json.optInt("channel");  // 👈 important

	                map.put("imagePath", filePath);
	                map.put("channel", ch);              // 👈 return channel also
	            }

	        } catch (Exception e) {
	            map.put("imagePath", null);
	        }

	        result.add(map);
	    }

	    return result;
	}

	@Transactional
	public void saveTerminalParams(TerminalParamsDto dto) {
		try {
			String json = objectMapper.writeValueAsString(dto);

			if (dto.getChannel() != null) {
				deviceCommandLogRepository.updateLatestResponseByDeviceAndChannel(dto.getDeviceId(), dto.getChannel(), json);
			} else {
				// fallback if channel not present
				deviceCommandLogRepository.updateLatestResponse(dto.getDeviceId(), json);
			}

		} catch (Exception e) {
			throw new RuntimeException("Error saving terminal params", e);
		}
	}



	@Transactional
	public List<Map<String, Object>> getTerminalConfigurationResponse(int deviceId) {

	    List<DeviceCommandLog> logs =
	            deviceCommandLogRepository.findLatestTerminalConfig((long) deviceId);

	    if (logs.isEmpty()) {
	        return List.of();
	    }

	    DeviceCommandLog latestLog = logs.get(0);
	    String response = latestLog.getResponse();

	    Map<String, Object> resultMap = new HashMap<>();

	    try {
	        ObjectMapper objectMapper = new ObjectMapper();

	        Map<String, Object> fullMap =
	                objectMapper.readValue(response, new TypeReference<Map<String, Object>>() {});

	        // ✅ Basic fields
	        resultMap.put("deviceId", fullMap.get("deviceId"));
	        resultMap.put("sim", fullMap.get("sim"));

	        // ✅ Extract decoded map
	        Map<String, Object> decoded =
	                (Map<String, Object>) fullMap.get("decoded");

	        if (decoded != null) {
	            for (Map.Entry<String, Object> entry : decoded.entrySet()) {

	                String key = entry.getKey();

	                // ❌ Skip hex keys & raw keys
	                if (key.matches("^[0-9A-Fa-f]+$") || key.endsWith("_raw")) {
	                    continue;
	                }

	                // ✅ Keep only readable keys
	                resultMap.put(key, entry.getValue());
	            }
	        }

	    } catch (Exception e) {
	        resultMap.put("deviceId", latestLog.getDeviceId());
	        resultMap.put("rawResponse", response);
	    }

	    return List.of(resultMap);
	}
}
