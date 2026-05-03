package com.trackingpath.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.trackingpath.dtos.SettingDTO;
import com.trackingpath.dtos.SettingLogoDTO;
import com.trackingpath.entities.Users;
import com.trackingpath.repositories.SettingRepository;

@Service
public class SettingService {

    @Autowired
    private SettingRepository settingRepo;

    public SettingDTO getSettingByUser(Users user) {
        return settingRepo.findById(user.getId())
                .map(this::convertToSettingBean)
                .orElse(null);
    }

    public boolean updateUserSetting(SettingDTO bean, Users user) {

        return settingRepo.findById(user.getId()).map(u -> {

            u.setServerName(bean.getServerName());
            u.setServerDescription(bean.getServerDescription());
            u.setLang_preferance(bean.getDefaultLanguage());
            u.setDefaultDateFormat(bean.getDefaultDateFormat());
            u.setDefaultTimeFormat(bean.getDefaultTimeFormat());
            u.setDefaultDurationFormat(bean.getDefaultDurationFormat());
            u.setDefaultUnitOfDistance(bean.getDefaultUnitOfDistance());
            u.setDefaultUnitOfCapacity(bean.getDefaultUnitOfCapacity());
            u.setDefaultUnitOfAltitude(bean.getDefaultUnitOfAltitude());
            u.setMapZoomLevel(bean.getMapZoomLevel());
            u.setLatitude(bean.getLatitude());
            u.setLongitude(bean.getLongitude());
            u.setNoReplyEmailAddress(bean.getNoReplyEmailAddress());
            u.setFromName(bean.getFromName());

            settingRepo.save(u);
            return true;
        }).orElse(false);
    }

    public boolean updateLogo(SettingLogoDTO bean, Users user) {

        return settingRepo.findById(user.getId()).map(u -> {

            u.setFrontpageLogo(bean.getFrontpageLogo());
            u.setFavicon(bean.getFavicon());
            u.setLoginPageLogo(bean.getLoginPageLogo());
            u.setBackgroundImage(bean.getBackgroundImage());
            u.setLoginTextColor(bean.getLoginTextColor());
            u.setLoginPanelColor(bean.getLoginPanelColor());
            u.setLoginPanelTransparency(bean.getLoginPanelTransparency());
            u.setWelcomeText(bean.getWelcomeText());
            u.setBottomText(bean.getBottomText());
            u.setAppleStoreLink(bean.getAppleStoreLink());
            u.setGooglePlayLink(bean.getGooglePlayLink());

            settingRepo.save(u);
            return true;
        }).orElse(false);
    }

    public boolean markWelcomeSeen(Users user) {

        return settingRepo.findById(user.getId()).map(u -> {
            u.setIsSeenByUser(true);
            settingRepo.save(u);
            return true;
        }).orElse(false);
    }

    public String getWelcomeText(Users user) {

        Users adminRecord = settingRepo.findByAdminIdAndIsSeenByUserFalse(user.getAdminId());
        return adminRecord != null ? adminRecord.getWelcomeText() : null;
    }

    /* Convert Entity to DTO */
    private SettingDTO convertToSettingBean(Users u) {
        SettingDTO dto = new SettingDTO();

        dto.setServerName(u.getServerName());
        dto.setServerDescription(u.getServerDescription());
        dto.setDefaultLanguage(u.getLang_preferance());
        dto.setDefaultDateFormat(u.getDefaultDateFormat());
        dto.setDefaultTimeFormat(u.getDefaultTimeFormat());
        dto.setDefaultDurationFormat(u.getDefaultDurationFormat());
        dto.setDefaultUnitOfDistance(u.getDefaultUnitOfDistance());
        dto.setDefaultUnitOfCapacity(u.getDefaultUnitOfCapacity());
        dto.setDefaultUnitOfAltitude(u.getDefaultUnitOfAltitude());
        dto.setLatitude(u.getLatitude());
        dto.setLongitude(u.getLongitude());
        dto.setMapZoomLevel(u.getMapZoomLevel());
        dto.setNoReplyEmailAddress(u.getNoReplyEmailAddress());
        dto.setFromName(u.getFromName());

        SettingLogoDTO logo = new SettingLogoDTO();
        logo.setFrontpageLogo(u.getFrontpageLogo());
        logo.setFavicon(u.getFavicon());
        logo.setLoginPageLogo(u.getLoginPageLogo());
        logo.setBackgroundImage(u.getBackgroundImage());
        logo.setLoginTextColor(u.getLoginTextColor());
        logo.setLoginPanelColor(u.getLoginPanelColor());
        logo.setLoginPanelTransparency(u.getLoginPanelTransparency());
        logo.setWelcomeText(u.getWelcomeText());
        logo.setBottomText(u.getBottomText());
        logo.setAppleStoreLink(u.getAppleStoreLink());
        logo.setGooglePlayLink(u.getGooglePlayLink());

        dto.setLogo(logo);
        return dto;
    }

	
}
