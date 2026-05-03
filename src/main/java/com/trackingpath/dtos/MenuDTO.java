package com.trackingpath.dtos;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class MenuDTO {
	private Long id;
	private String menuName;
	private String url;
	private List<MenuDTO> children = new ArrayList<>();
}
