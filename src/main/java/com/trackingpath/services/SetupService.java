package com.trackingpath.services;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.trackingpath.dtos.CustomUserDTO;
import com.trackingpath.dtos.DriverSetupBean;
import com.trackingpath.dtos.SetupDeviceDTO;
import com.trackingpath.dtos.SetupTemplateBean;
import com.trackingpath.entities.DeviceEntity;
import com.trackingpath.entities.Driveres;
import com.trackingpath.entities.Routes;
import com.trackingpath.entities.SetupTemplateEntity;
import com.trackingpath.entities.Users;
import com.trackingpath.repositories.DeviceRepository;
import com.trackingpath.repositories.DriverRepository;
import com.trackingpath.repositories.SetupTemplateRepository;
import com.trackingpath.repositories.UserRepository;

@Service
public class SetupService {

	@Autowired
	DriverRepository driverRepository;
	@Autowired
	DeviceRepository deviceRepository;
	@Autowired
	SetupTemplateRepository setupTemplateRepository;
	@Autowired
	UserRepository userRepository;
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	public boolean adddriverData(DriverSetupBean bean, Users user) {
		try {
			Driveres entity = new Driveres();

			entity.setUserId(user.getId());
			entity.setAdminId(user.getAdminId());
			entity.setName(bean.getName());
			entity.setRfid(bean.getRfid());
			entity.setPhone(bean.getPhone());
			entity.setEmail(bean.getEmail());
			entity.setDescription(bean.getDescription());
			entity.setUsername(bean.getUsername());
			 if (bean.getPassword() != null && !bean.getPassword().isEmpty()) {
		            entity.setPassword(passwordEncoder.encode(bean.getPassword()));
		        }

			if (bean.getDeviceId() != null) {
				DeviceEntity device = deviceRepository.findById(bean.getDeviceId()).orElse(null);
				entity.setDevice(device);
			}

			if (bean.getCurrentDeviceId() != null) {
				DeviceEntity currentDevice = deviceRepository.findById(bean.getCurrentDeviceId()).orElse(null);
				entity.setCurrentDevice(currentDevice);
			}

			driverRepository.save(entity);
			return true;

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public Map<String, Object> getDriverData(Users user, int page, int size, String search) {
		Pageable pageable = PageRequest.of(page, size);

		String searchParam = "%" + (search != null ? search.trim() : "") + "%";

		Page<DriverSetupBean> driverPage;

		if (user.getRoles().stream().anyMatch(r -> "ROLE_ADMIN".equalsIgnoreCase(r.getRoleName()))) {
			driverPage = driverRepository.findDriversByAdmin(user.getAdminId(), searchParam, pageable);
		} else {
			driverPage = driverRepository.findDriversByUser(user.getId(), searchParam, pageable);
		}

		Map<String, Object> response = new HashMap<>();
		response.put("data", driverPage.getContent());
		response.put("currentPage", driverPage.getNumber());
		response.put("totalItems", driverPage.getTotalElements());
		response.put("totalPages", driverPage.getTotalPages());

		return response;
	}

	public boolean updateDriverData(DriverSetupBean bean, Users user) {
		try {
			Driveres driver = driverRepository.findById(bean.getId()).orElse(null);

			if (driver == null) {
				return false;
			}
			boolean isAdmin = user.getRoles().stream().anyMatch(r -> "ROLE_ADMIN".equalsIgnoreCase(r.getRoleName()));

			if (isAdmin) {
				if (user.getAdminId() != driver.getAdminId()) {
					return false;
				}
			} else {
				if (!user.getId().equals(driver.getUserId())) {
					return false;
				}
			}

			driver.setName(bean.getName());
			driver.setRfid(bean.getRfid());
			driver.setPhone(bean.getPhone());
			driver.setEmail(bean.getEmail());
			driver.setDescription(bean.getDescription());
			driver.setUsername(bean.getUsername());
			driver.setPassword(bean.getPassword());
			if (bean.getPassword() != null && !bean.getPassword().isEmpty()) {
				driver.setPassword(passwordEncoder.encode(bean.getPassword()));
	        }
			if (driver.getActive() == null) {
			    driver.setActive(true); // or false based on your default logic
			}

			if (bean.getDeviceId() != null) {
				driver.setDevice(deviceRepository.findById(bean.getDeviceId()).orElse(null));
			}

			if (bean.getCurrentDeviceId() != null) {
				driver.setCurrentDevice(deviceRepository.findById(bean.getCurrentDeviceId()).orElse(null));
			}

			driverRepository.save(driver);
			return true;

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean deleteDriver(long driverId, Users user) {

		try {
			Driveres driver = driverRepository.findById(driverId).orElse(null);
			if (driver == null)
				return false;

			boolean isAdmin = user.getRoles().stream().anyMatch(r -> "ROLE_ADMIN".equalsIgnoreCase(r.getRoleName()));

			if (isAdmin) {
				if (user.getAdminId() != driver.getAdminId()) {
					return false;
				}
			} else {
				if (!user.getId().equals(driver.getUserId())) {
					return false;
				}
			}

			driverRepository.delete(driver);
			return true;

		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
	}

	public Map<String, Object> getSmsData(Users user, int page, int size, String search) {

		String searchParam = "%" + (search != null ? search.trim() : "") + "%";

		boolean isAdmin = user.getRoles().stream().anyMatch(r -> "ROLE_ADMIN".equalsIgnoreCase(r.getRoleName()));
		Pageable pageable = PageRequest.of(page, size);

		Page<SetupTemplateBean> smsPage = setupTemplateRepository.findSmsTemplates(isAdmin, user.getAdminId(),
				user.getId(), searchParam, pageable);

		Map<String, Object> response = new HashMap<>();
		response.put("data", smsPage.getContent());
		response.put("currentPage", smsPage.getNumber());
		response.put("totalItems", smsPage.getTotalElements());
		response.put("totalPages", smsPage.getTotalPages());

		return response;
	}

	public Map<String, Object> getEmailData(Users user, int page, int size, String search) {

		String searchParam = "%" + (search != null ? search.trim() : "") + "%";

		boolean isAdmin = user.getRoles().stream().anyMatch(r -> "ROLE_ADMIN".equalsIgnoreCase(r.getRoleName()));
		Pageable pageable = PageRequest.of(page, size);

		Page<SetupTemplateBean> emailPage = setupTemplateRepository.findEmailTemplates(isAdmin, user.getAdminId(),
				user.getId(), searchParam, pageable);

		Map<String, Object> response = new HashMap<>();
		response.put("data", emailPage.getContent());
		response.put("currentPage", emailPage.getNumber());
		response.put("totalItems", emailPage.getTotalElements());
		response.put("totalPages", emailPage.getTotalPages());

		return response;
	}

	public Map<String, Object> getGprsData(Users user, int page, int size, String search) {

		String searchParam = "%" + (search != null ? search.trim() : "") + "%";
		boolean isAdmin = user.getRoles().stream().anyMatch(r -> "ROLE_ADMIN".equalsIgnoreCase(r.getRoleName()));
		Pageable pageable = PageRequest.of(page, size);

		Page<SetupTemplateBean> gprsPage = setupTemplateRepository.findGprsTemplates(isAdmin, user.getAdminId(),
				user.getId(), searchParam, pageable);

		Map<String, Object> response = new HashMap<>();
		response.put("data", gprsPage.getContent());
		response.put("currentPage", gprsPage.getNumber());
		response.put("totalItems", gprsPage.getTotalElements());
		response.put("totalPages", gprsPage.getTotalPages());

		return response;
	}

	public boolean addsmsData(SetupTemplateBean bean, Users user) {
		try {
			SetupTemplateEntity entity = new SetupTemplateEntity();

			entity.setUserId(user.getId());
			entity.setAdminId(user.getAdminId());
			entity.setTitle(bean.getTitle());
			entity.setAdapted(bean.getAdapted());
			entity.setSubject(bean.getSubject());
			entity.setMessage(bean.getMessage());
			entity.setCategory("SMS");

			setupTemplateRepository.save(entity);
			return true;

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean updatesmsData(SetupTemplateBean bean, Users user) {
		try {
			SetupTemplateEntity sms = setupTemplateRepository.findById(bean.getId()).orElse(null);

			if (sms == null) {
				return false;
			}
			boolean isAdmin = user.getRoles().stream().anyMatch(r -> "ROLE_ADMIN".equalsIgnoreCase(r.getRoleName()));

			if (isAdmin) {
				if (user.getAdminId() != sms.getAdminId()) {
					return false;
				}
			} else {
				if (!user.getId().equals(sms.getUserId())) {
					return false;
				}
			}

			sms.setTitle(bean.getTitle());
			sms.setAdapted(bean.getAdapted());
			sms.setSubject(bean.getSubject());
			sms.setMessage(bean.getMessage());

			setupTemplateRepository.save(sms);
			return true;

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean addemailData(SetupTemplateBean bean, Users user) {
		try {
			SetupTemplateEntity entity = new SetupTemplateEntity();

			entity.setUserId(user.getId());
			entity.setAdminId(user.getAdminId());
			entity.setTitle(bean.getTitle());
			entity.setAdapted(bean.getAdapted());
			entity.setSubject(bean.getSubject());
			entity.setMessage(bean.getMessage());
			entity.setTemplateName(bean.getTemplateName());
			entity.setCategory("EMAIL");

			setupTemplateRepository.save(entity);
			return true;

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean updateEmailData(SetupTemplateBean bean, Users user) {
		try {
			SetupTemplateEntity email = setupTemplateRepository.findById(bean.getId()).orElse(null);

			if (email == null) {
				return false;
			}
			boolean isAdmin = user.getRoles().stream().anyMatch(r -> "ROLE_ADMIN".equalsIgnoreCase(r.getRoleName()));

			if (isAdmin) {
				if (user.getAdminId() != email.getAdminId()) {
					return false;
				}
			} else {
				if (!user.getId().equals(email.getUserId())) {
					return false;
				}
			}

			email.setTitle(bean.getTitle());
			email.setAdapted(bean.getAdapted());
			email.setSubject(bean.getSubject());
			email.setMessage(bean.getMessage());
			email.setTemplateName(bean.getTemplateName());

			setupTemplateRepository.save(email);
			return true;

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean addgprsData(SetupTemplateBean bean, Users user) {
		try {
			SetupTemplateEntity entity = new SetupTemplateEntity();

			entity.setUserId(user.getId());
			entity.setAdminId(user.getAdminId());
			entity.setTitle(bean.getTitle());
			entity.setAdapted(bean.getAdapted());
			entity.setSubject(bean.getSubject());
			entity.setMessage(bean.getMessage());
			entity.setCategory("GPRS");

			setupTemplateRepository.save(entity);
			return true;

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean updategprsData(SetupTemplateBean bean, Users user) {
		try {
			SetupTemplateEntity gprs = setupTemplateRepository.findById(bean.getId()).orElse(null);

			if (gprs == null) {
				return false;
			}
			boolean isAdmin = user.getRoles().stream().anyMatch(r -> "ROLE_ADMIN".equalsIgnoreCase(r.getRoleName()));

			if (isAdmin) {
				if (user.getAdminId() != gprs.getAdminId()) {
					return false;
				}
			} else {
				if (!user.getId().equals(gprs.getUserId())) {
					return false;
				}
			}

			gprs.setTitle(bean.getTitle());
			gprs.setAdapted(bean.getAdapted());
			gprs.setSubject(bean.getSubject());
			gprs.setMessage(bean.getMessage());

			setupTemplateRepository.save(gprs);
			return true;

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean deleteTemplate(long Id, Users user) {

		try {
			SetupTemplateEntity temp = setupTemplateRepository.findById(Id).orElse(null);
			if (temp == null)
				return false;

			boolean isAdmin = user.getRoles().stream().anyMatch(r -> "ROLE_ADMIN".equalsIgnoreCase(r.getRoleName()));

			if (isAdmin) {
				if (user.getAdminId() != temp.getAdminId()) {
					return false;
				}
			} else {
				if (!user.getId().equals(temp.getUserId())) {
					return false;
				}
			}

			setupTemplateRepository.delete(temp);
			return true;

		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
	}

	@Transactional
	public boolean updateuserdatasetup(CustomUserDTO bean, Users sessionUser) {

		Users user = userRepository.findById(sessionUser.getId()).orElse(null);

		if (user == null) {
			return false;
		}

		if (bean.getSmsGatewayType() != null)
			user.setSmsGatewayType(bean.getSmsGatewayType());

		if (bean.getSmsGatewayUrl() != null)
			user.setSmsGatewayUrl(bean.getSmsGatewayUrl());

		if (bean.getSmtpHost() != null)
			user.setSmtpHost(bean.getSmtpHost());

		if (bean.getSmtpUsername() != null)
			user.setSmtpUsername(bean.getSmtpUsername());

		if (bean.getSmtpPassword() != null)
			user.setSmtpPassword(bean.getSmtpPassword());

		if (bean.getSmtpPort() != null)
			user.setSmtpPort(bean.getSmtpPort());

		if (bean.getSmtpEncryption() != null)
			user.setSmtpEncryption(bean.getSmtpEncryption());

		if (bean.getAvailableWidgets() != null)
			user.setAvailableWidgets(bean.getAvailableWidgets());

		if (bean.getDashboardMenu() != null)
			user.setDashboardMenu(bean.getDashboardMenu());

		userRepository.save(user);
		return true;
	}

	public Map<String, Object> getObjectData(Users user, int page, int size, String search) {

		String searchParam = "%" + (search != null ? search.trim() : "") + "%";

		boolean isAdmin = user.getRoles().stream().anyMatch(r -> "ROLE_ADMIN".equalsIgnoreCase(r.getRoleName()));

		Pageable pageable = PageRequest.of(page, size);

		List<Long> deviceIds = List.of();

		if (!isAdmin) {
			if (user.getAssign_device_ids() != null && !user.getAssign_device_ids().isBlank()) {

				deviceIds = Arrays.stream(user.getAssign_device_ids().split(",")).map(String::trim).map(Long::valueOf)
						.toList();
			}

			if (deviceIds.isEmpty()) {
				return Map.of("data", List.of(), "currentPage", page, "totalItems", 0, "totalPages", 0);
			}
		}

		Page<SetupDeviceDTO> object = deviceRepository.findObjectData(isAdmin, deviceIds, searchParam, pageable);

		return Map.of("data", object.getContent(), "currentPage", object.getNumber(), "totalItems",
				object.getTotalElements(), "totalPages", object.getTotalPages());
	}

	public Map<Long, String> getAllDriver() {

		return driverRepository.findAllDriver().stream().filter(d -> d.getId() != null && d.getName() != null)
				.collect(Collectors.toMap(Driveres::getId, Driveres::getName));

	}
}
