package com.trackingpath.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParentSummaryDto {
    private Long parentId;
    private Long passengerLoginId;
    private String name;
    private String mobile;
    private String email;
}
