package com.trackingpath.entities;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "device_command_log")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeviceCommandLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "device_id")
    private Long deviceId;

    @Column(name = "command_name")
    private String commandName;

    @Column(name = "command_msg")
    private String commandMsg;

    @Column(name = "created_on")
    private LocalDateTime createdOn;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "admin_id")
    private Long adminId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id", insertable = false, updatable = false)
    private DeviceEntity device;
    @Column(name = "command_category")
    private String commandCategory;

    @Column(name = "command_sub_category")
    private String commandSubCategory;
    
    @Column(name = "response", columnDefinition = "TEXT")
    private String response;
    
    @Column(name = "channel_id")
    private Integer channelId;
}