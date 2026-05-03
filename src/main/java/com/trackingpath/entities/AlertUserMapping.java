package com.trackingpath.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "alert_users_mapping")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertUserMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long user_id;
    private Long admin_id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "alert_id")
    private Alert alert;
}

