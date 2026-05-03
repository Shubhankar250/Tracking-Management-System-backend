package com.trackingpath.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

	@Entity
	@Table(name = "report_master")
	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public class ReportMaster {

	    @Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    private Integer id;

	    @Column(name = "report_type", length = 100)
	    private String reportType;

	    @Column(name = "report_column", length = 255)
	    private String reportColumn;

	}


