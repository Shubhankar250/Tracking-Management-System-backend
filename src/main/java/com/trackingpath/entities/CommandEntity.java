package com.trackingpath.entities;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "command_data")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommandEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String model;
  
    @Column(name = "command_name")
    private String commandName;

    @Column(name = "command_code")
    private String commandCode;
    @CreationTimestamp
    @Column(name = "createdOn")
    private LocalDateTime createdOn;  // DB default: now()

    @Column(name = "command_status")
    private Short commandStatus;      // smallint → Short

    private String types;
    @Column(name = "command_category")
    private String commandCategory;

    @Column(name = "command_sub_category")
    private String commandSubCategory;

}

