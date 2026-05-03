package com.trackingpath.entities;

import java.util.List;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.trackingpath.dtos.SlotBean;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "share_position_schedule")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SharePositionScheduleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<SlotBean> data;

    @Column(name = "share_id")
    private Long shareId;

    @Column(name = "admin_id")	
    private Long adminId;

    @Column(name = "user_id")
    private Long userId;
}
