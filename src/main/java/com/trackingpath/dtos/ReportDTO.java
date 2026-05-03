package com.trackingpath.dtos;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportDTO {
	    private Long id;
	    private String from_date;
	    private String to_date;

	    private String title;
	    private String type;
	    private String format;
	    private String period;
	    private String emails;
	    private String speed_limit;
	    private String stops;
	    private String daily;
	    private String weekly;
	    private String monthly;

	    private List<String> skip_column;
	    private List<Long> devices;
	    private List<Long> geofences;
	    private Long admin_id;
	    private Long user_id;
	    // ✅ REQUIRED for JPQL constructor projection
	    public ReportDTO(Long id, String title, String type, String format, String period) {
	        this.id = id;
	        this.title = title;
	        this.type = type;
	        this.format = format;
	        this.period = period;
	    }
	}