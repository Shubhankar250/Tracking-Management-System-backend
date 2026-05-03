package com.trackingpath.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SetupTemplateBean {

	private long id;
	private String title;
	private String adapted;
	private String message;
	private String subject;
	private String category;
	private String templateName;

}
