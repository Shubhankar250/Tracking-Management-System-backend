package com.trackingpath.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "device_modal")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeviceModalEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "company_name")
	private String companyName;

	@Column(name = "modal_name")
	private String modalName;

	@Column(name = "modal_type")
	private String modalType;

	@Column(name = "no_of_channel")
	private Long noOfChannel;

	@Column(name = "image")
	private String image;

	@Column(name = "user_mannual")
	private String userManual;

	@Column(name = "protocol_mannual")
	private String protocolManual;

	@Column(name = "commands")
	private String commands;

	@Column(name = "connected_ip")
	private String connectedIP;

	@Column(name = "connected_port")
	private String connectedPort;
	
	@Column(name = "no_of_din")
	private Long noOfDIN;
	
	@Column(name = "no_of_ain")
	private Long noOfAIN;
	
	@Column(name = "no_of_dout")
	private Long noOfDOUT;
	
	@Column(name = "protocol_name")
	private String protocolName;
	
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
	private Users userId;
	
	@Column(name = "admin_id")
	private Long adminId;
	
	@Column(name = "adas_alert_type")
    private String adasAlertType;
    
    @Column(name = "dms_alert_type")
    private String dmsAlertType;
    @Column(name = "is_active")
    private Boolean active;
	
	
}
