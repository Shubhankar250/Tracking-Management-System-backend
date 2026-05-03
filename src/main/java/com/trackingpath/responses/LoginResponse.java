// src/main/java/com/trackingpath/models/LoginResponse.java
package com.trackingpath.responses;

import java.util.List;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class LoginResponse {
	private long id;
    private String token;
    private long expiresIn;
    private List<String> roles;
    private String zlm_token;
    private String url;
    private String username;

   
}
