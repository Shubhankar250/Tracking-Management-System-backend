package com.trackingpath;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.trackingpath.security.JwtProperties;


@SpringBootApplication(scanBasePackages = "com.trackingpath")
@EnableConfigurationProperties(JwtProperties.class)
public class TP_FLEET_PLUS_BOOT {

  public static void main(String[] args) {
    SpringApplication.run(TP_FLEET_PLUS_BOOT.class, args);
  }

}
