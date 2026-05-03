package com.trackingpath.dtos;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParentLoginResponse {
    private String token;
    private String tokenType;
    private long expiresInSeconds;
    private ParentSummaryDto parent;
    private List<ChildBasicDto> children;
}
