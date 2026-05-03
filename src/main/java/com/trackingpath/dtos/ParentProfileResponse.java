package com.trackingpath.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParentProfileResponse {
    private Long parentId;
    private String name;
    private String mobile;
    private String email;
    private Integer childrenCount;
}
