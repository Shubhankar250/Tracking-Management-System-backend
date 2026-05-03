package com.trackingpath.entities;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "module_logtype_mapping")
@Data
public class ModuleLogTypeMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "module", nullable = false, length = 50)
    private String module;

    @Column(name = "log_types", nullable = false, columnDefinition = "text")
    private String logTypes;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

   
}
