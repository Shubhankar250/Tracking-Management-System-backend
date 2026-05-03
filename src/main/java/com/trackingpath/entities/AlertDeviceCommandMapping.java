package com.trackingpath.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "alert_device_command_mapping")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertDeviceCommandMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String commandName;
    private Long userId;
    private Long adminId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "alert_id")
    private Alert alert;
}


