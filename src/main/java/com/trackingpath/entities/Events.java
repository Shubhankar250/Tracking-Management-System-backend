package com.trackingpath.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "events")
public class Events {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(name = "user_id")
	private Integer userId;

	@Column(name = "alert_time")
	private LocalDateTime alertTime;

	@Column(name = "alert_type")
	private String alertType;

	@Column(name = "alert_name")
	private String alertName;
	private String message;

	private Double latitude;
	private Double longitude;
	private String address;

	private LocalDateTime sending_time;
	private LocalDateTime servertime;


    @Column(name = "course")
    private Double course;

    @Column(name = "attributes")
    private String attributes;

    @Column(name = "altitude")
    private Double altitude;

    @Column(name = "speed")
    private Double speed;
	/* Optional relation */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "device_id", insertable = false, updatable = false)
	private DeviceEntity device;

}
