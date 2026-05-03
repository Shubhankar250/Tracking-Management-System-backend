package com.trackingpath.dtos;

import lombok.Data;

@Data
public class ExternalAccessTokenDTO {

    private Long id;
    private String username;
    private String password;
    private String projectName;
    private String externalAccessToken;
    private String url;
}
