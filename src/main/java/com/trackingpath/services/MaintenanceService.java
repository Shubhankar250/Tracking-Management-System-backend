package com.trackingpath.services;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.trackingpath.dtos.MaintenanceAllDataView;
import com.trackingpath.dtos.MaintenanceDto;
import com.trackingpath.dtos.MaintenanceResponseDto;
import com.trackingpath.dtos.MaintenanceServiceDto;
import com.trackingpath.entities.DeviceEntity;
import com.trackingpath.entities.MaintenanceEntity;
import com.trackingpath.entities.Users;
import com.trackingpath.mapper.MaintenanceMapper;
import com.trackingpath.repositories.DeviceRepository;
import com.trackingpath.repositories.MaintenanceRepository;
import com.trackingpath.repositories.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@Service
public class MaintenanceService {

	@Autowired
	private DeviceRepository deviceRepo;

	@Autowired
	private UserRepository usersRepo;

	@Autowired
	private MaintenanceRepository repository;

	@Autowired
	private MaintenanceMapper mapper;

	public boolean addMaintenanceData(MaintenanceDto dto, Users user) {

		DeviceEntity device = deviceRepo.findById(dto.getDeviceId())
				.orElseThrow(() -> new RuntimeException("Device not found"));

		Users username = usersRepo.findById(user.getId()).orElseThrow(() -> new RuntimeException("User not found"));

		Users admin = usersRepo.findById(user.getAdminId()).orElseThrow(() -> new RuntimeException("Admin not found"));

		MaintenanceEntity entity = MaintenanceMapper.toEntity(dto, device, username, admin);

		repository.save(entity);

		return true;
	}

	public Map<String, Object> getMaintenanceData(Users user, int page, int pageSize, String search) {

	    Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by("id").descending());

	    Page<MaintenanceAllDataView> maintenancePage =
	            repository.findAllMaintenance(user.getId(), search, pageable);

	    List<MaintenanceResponseDto> dtoList =
	            mapper.toDtoListView(maintenancePage.getContent());

	    Map<String, Object> res = new HashMap<>();
	    res.put("data", dtoList);
	    res.put("totalRecords", maintenancePage.getTotalElements());

	    return res;
	}


	public boolean changeMaintenanceStatus(long id, String status, Users user) {

		Optional<MaintenanceEntity> opt = repository.findById(id);

		if (opt.isEmpty()) {
			return false;
		}

		MaintenanceEntity entity = opt.get();

		// Toggle logic
		if ("true".equals(status)) {
			entity.setEventTrigger(false);
		} else if ("false".equals(status)) {
			entity.setEventTrigger(true);
		} else {
			return false;
		}

		repository.save(entity);
		return true;
	}

	public MaintenanceResponseDto getById(Long id) {
		MaintenanceEntity entity = repository.findByIdWithRelations(id)
				.orElseThrow(() -> new RuntimeException("Maintenance record not found"));

		return mapper.toDto(entity);
	}

	public boolean updateMaintenanceData(MaintenanceDto bean, Users user) {

		MaintenanceEntity entity = repository.findByIdWithRelations(bean.getId())
				.orElseThrow(() -> new RuntimeException("Maintenance data not found"));

		// UPDATE FIELDS IF NOT NULL
		if (bean.getServiceName() != null)
			entity.setServiceName(bean.getServiceName());

		entity.setDatalist(bean.isDatalist());
		entity.setPopup(bean.isPopup());

		entity.setOdometerIntervalKm(bean.isOdometerIntervalKm());
		if (bean.getOdometerLeftKmVal() != 0)
			entity.setOdometerIntervalKmVal(bean.getOdometerLeftKmVal());

		if (bean.getLastServiceKm() != 0)
			entity.setLastServiceKm(bean.getLastServiceKm());

		entity.setEngineHourInterval(bean.isEngineHourInterval());
		if (bean.getEngineHourIntervalVal() != 0)
			entity.setEngineHourIntervalVal(bean.getEngineHourIntervalVal());

		if (bean.getLastServiceHours() != 0)
			entity.setLastServiceHours(bean.getLastServiceHours());

		entity.setDaysInterval(bean.isDaysInterval());
		if (bean.getDaysIntervalVal() != 0)
			entity.setDaysIntervalVal(bean.getDaysIntervalVal());

		if (bean.getLastServiceDate() != null && !bean.getLastServiceDate().isEmpty())
			entity.setLastServiceDate(LocalDate.parse(bean.getLastServiceDate())); // yyyy-MM-dd

		entity.setOdometerLeftKm(bean.isOdometerLeftKm());
		if (bean.getOdometerLeftKmVal() != 0)
			entity.setOdometerLeftKmVal(bean.getOdometerLeftKmVal());

		entity.setEngineHoursLeft(bean.isEngineHoursLeft());
		if (bean.getEngineHoursLeftVal() != null && bean.getEngineHoursLeftVal() != 0)
			entity.setEngineHoursLeftVal(bean.getEngineHoursLeftVal());

		entity.setDaysLeft(bean.isDaysLeft());
		if (bean.getDaysLeftVal() != 0)
			entity.setDaysLeftVal(bean.getDaysLeftVal());

		entity.setUpdateLastService(bean.isUpdateLastService());

		if (bean.getDeviceId() != null && bean.getDeviceId() > 0) {
			DeviceEntity device = deviceRepo.findById(bean.getDeviceId())
					.orElseThrow(() -> new RuntimeException("Device not found"));
			entity.setDevice(device);
		}

		repository.save(entity);
		return true;
	}

	public boolean deleteMaintenanceService(Long id, Users loginUser) {
		if (repository.existsById(id)) {
			repository.deleteById(id);
			return true;
		}
		return false;
	}

	public List<MaintenanceServiceDto> getMaintenanceMultipleDataById(Users user, long deviceId) {

		return repository.findMaintenanceByDeviceId(deviceId);
	}

}