package com.trackingpath.controllers;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trackingpath.dtos.SettingDTO;
import com.trackingpath.dtos.SettingLogoDTO;
import com.trackingpath.entities.Users;
import com.trackingpath.services.AuthenticationService;
import com.trackingpath.services.SettingService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/settings")
public class SettingController {

    @Autowired
    private SettingService settingService;

    @Autowired
    private AuthenticationService authenticationService;

    @Value("${FILE_UPLOAD_PATH}")
    private String FILE_UPLOAD_PATH;

	
	  @GetMapping("") 
	  public SettingDTO getsettingDataById() {
		  Users user = authenticationService.getUser();
	  SettingDTO settings = settingService.getSettingByUser(user);
	  return settings;
	  
	  }
	  
	 
    
    
    // UPDATE SETTINGS
    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public String updateSettings(@RequestBody SettingDTO bean,
                                 HttpServletRequest request) {

        Users user = authenticationService.getUser();

        boolean status = settingService.updateUserSetting(bean, user);

        if (status) {
            user.setLang_preferance(bean.getDefaultLanguage());
            return "Updated successfully";
        }
        return "Failed!";
    }

    // UPDATE LOGOS / IMAGES
    @PutMapping(value = "/logos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String updateLogo(@RequestParam("data") String data,
                             @RequestParam(required = false) MultipartFile frontpageLogo,
                             @RequestParam(required = false) MultipartFile favicon,
                             @RequestParam(required = false) MultipartFile loginPageLogo,
                             @RequestParam(required = false) MultipartFile backgroundImage)
            throws IOException {

        Users user = authenticationService.getUser();
        SettingLogoDTO bean = new ObjectMapper().readValue(data, SettingLogoDTO.class);

        long maxSize = 5 * 1024 * 1024;

        if (frontpageLogo != null) bean.setFrontpageLogo(saveImage(frontpageLogo, maxSize));
        if (favicon != null) bean.setFavicon(saveImage(favicon, maxSize));
        if (loginPageLogo != null) bean.setLoginPageLogo(saveImage(loginPageLogo, maxSize));
        if (backgroundImage != null) bean.setBackgroundImage(saveImage(backgroundImage, maxSize));

        return settingService.updateLogo(bean, user)
                ? "Details updated successfully"
                : "Something went wrong";
    }

    // WELCOME VIEW
    @PostMapping("/welcome")
    public String updateWelcomeView() {

        Users user = authenticationService.getUser();
        String welcomeText = settingService.getWelcomeText(user);

        if (welcomeText != null) {
            settingService.markWelcomeSeen(user);
            return "Welcome view updated";
        }
        return "No welcome text";
    }

    private String saveImage(MultipartFile file, long maxSize) throws IOException {

        if (file.getSize() > maxSize) return null;

        String fileName = file.getOriginalFilename();
        if (fileName == null || !fileName.contains(".")) return null;

        String ext = fileName.substring(fileName.lastIndexOf("."));
        String newName = UUID.randomUUID() + ext;

        File dest = new File(FILE_UPLOAD_PATH, newName);
        dest.getParentFile().mkdirs();
        file.transferTo(dest);

        return newName;
    }
}
