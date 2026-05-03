package com.trackingpath.dtos;

public class DeviceInfo {

    private String iconType;
    private String imgIconName;
    private String name;
    private String status;
    private String lastidletime;
    private String lastmovementtime;
    private String devicetimezone;
    

    
    public String getDevicetimezone() {
		return devicetimezone;
	}

	/*
	 * public void setDevicetimezone(String devicetimezone) { this.devicetimezone =
	 * devicetimezone; }
	 */

	public DeviceInfo(
            String iconType,
            String imgIconName,
            String name,
            String status,
            String lastidletime,
            String lastmovementtime,
            String devicetimezone
    ) {
        this.iconType = iconType;
        this.imgIconName = imgIconName;
        this.name = name;
        this.status = status;
        this.lastidletime = lastidletime;
        this.lastmovementtime = lastmovementtime;
        this.devicetimezone=devicetimezone;
    }

    public String getIconType() {
        return iconType;
    }

    public String getImgIconName() {
        return imgIconName;
    }

    public String getName() {
        return name;
    }

    public String getStatus() {
        return status;
    }

    public String getLastidletime() {
        return lastidletime;
    }

    public String getLastmovementtime() {
        return lastmovementtime;
    }
}
