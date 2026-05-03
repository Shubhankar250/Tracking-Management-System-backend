package com.trackingpath.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "external_access_token")
@Data
public class ExternalAccessToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String password;

    @Column(name = "project_name", unique = true)
    private String projectName;

    @Column(name = "external_access_token")
    private String externalAccessToken;

    private String url;
    // getters setters
}
