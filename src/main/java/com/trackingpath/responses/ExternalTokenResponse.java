package com.trackingpath.responses;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ExternalTokenResponse {

    private String projectName;
    private String url;
    private String token;
}