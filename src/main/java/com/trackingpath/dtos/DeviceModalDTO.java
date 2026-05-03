package com.trackingpath.dtos;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeviceModalDTO {

	    private Long id;
	    private String companyName;
	    private String modalName;
	    private String modalType;
	    private Long noOfChannel;
	    private String image;
	    private String userManual;
	    private String protocolManual;
	    private String commands;
	    private String connectedIP;
	    private String connectedPort;
	    private Long noOfDIN;
	    private Long noOfAIN;
	    private Long noOfDOUT;
	    private String protocolName;
	    private String adasAlertType;
	    private String dmsAlertType;
	    private Boolean active;
}
	

