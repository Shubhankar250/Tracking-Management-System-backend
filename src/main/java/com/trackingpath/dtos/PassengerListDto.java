package com.trackingpath.dtos;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PassengerListDto {
	    private String passengerName;
	    private Long passengerId;
	    private String pickupStop;
	    private String guardianName;
	    private String guardianNumber;
	    private String className;
	    private Long rollNumber;
	    private String expectedTime;
	    private String attendanceStatus;
	    private String boardingStatus;
	    private String deboardingStatus;
	    private Boolean guardianVerified;
	    private Boolean guardianInformed;
}
