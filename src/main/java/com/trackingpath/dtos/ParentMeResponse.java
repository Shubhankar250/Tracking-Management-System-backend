package com.trackingpath.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParentMeResponse {
    private Long parentId;
    private String username;
    private String role;
    private String name;
    private String mobile;
    private String email;
}
