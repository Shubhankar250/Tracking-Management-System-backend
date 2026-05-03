package com.trackingpath.dtos;

import lombok.Data;

@Data
public class LoginUserDto {
    private String username;
    private String password;

 
    @Override
    public String toString() {
        return "LoginUserDto{" +
                "username='" + username + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
