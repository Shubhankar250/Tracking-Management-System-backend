package com.trackingpath.dtos;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponseExternal {

	private boolean successful;
	private String message;
	private List<LiveDataResponseDTO> object;
}
