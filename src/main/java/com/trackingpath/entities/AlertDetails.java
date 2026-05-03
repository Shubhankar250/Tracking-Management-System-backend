package com.trackingpath.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "alert_details")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "alert")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class AlertDetails {

    @EqualsAndHashCode.Include
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "alert_type")
    private String alertType;

	@Column(name = "overspeed")
	private Double overspeed;

	@Column(name = "stop_duration")
	private Long stopDuration;

	@Column(name = "idle_duration")
	private Long idleDuration;
	
	@Column(name = "ignition")
	private String ignition;

	@Column(name = "sos")
	private Boolean sos;
	
	// =========================
    // OWNING SIDE
    // =========================
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "alert_id", nullable = false, unique = true)
    private Alert alert;
    
    @Column(name = "admin_id")
    private Long adminId;
    
    @Column(name = "user_id")
	private Long userId;
    
	@Column(name = "low_speed")
    private Double lowSpeed;
	
	@Column(name = "driver_change_ids")
	private String driverChangeIds;
    
	@Column(name = "driver_change_auth")
	private Boolean driverChangeAuth;
	
	@Column(name = "poi_stop_duration")
	private Integer poiStopDuration;
    
	@Column(name = "poi_idle_duration")
	private Integer poiIdleDuration;
	
	@Column(name = "vibration")
	private Boolean vibration;
    
	@Column(name = "movement")
	private Boolean movement;
    
	@Column(name = "falldown")
	private Boolean falldown;
    
	@Column(name = "lowpower")
	private Boolean lowpower;
    
	@Column(name = "lowbattery")
	private Boolean lowbattery;
    
	@Column(name = "powercut")
	private Boolean powercut;
    
	@Column(name = "powerrestored")
	private Boolean powerrestored;
	
	@Column(name = "adas_dms_category")
	private String adasDmsCategory;
	
	
	
    
}
