package com.trackingpath.entities;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "template_data")
@Data
public class SetupTemplateEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String title;
	private String adapted;
	private String subject;
	private String message;

	@Column(name = "user_id")
	private Long userId;

	private String category;

	@Column(name = "admin_id")
	private Long adminId;

	@CreationTimestamp
	@Column(name = "created_on")
	private LocalDateTime createdOn;

	@Column(name = "template_name")
	private String templateName;

}
