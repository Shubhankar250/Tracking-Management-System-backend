package com.trackingpath.entities;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import lombok.ToString;

@Entity
@Table(name = "alerts")
@Data
@ToString(exclude = {
    "deviceMappings", "geofenceMappings", "routeMappings",
    "alertDetails", "poiMappings", "deviceCommand",
    "schedules", "notifications", "alertUsers"
})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Alert {

	@EqualsAndHashCode.Include
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
    private String alertName;

    private Long userId;

    private Long adminId;

    private String status;
    
	/*
	 * @Version
	 * 
	 * @Column(nullable = false) private Long version = 0L;
	 */



    @Column(name = "created_on", updatable = false)
    private LocalDateTime createdOn;

    @PrePersist
    public void onCreate() {
        this.createdOn = LocalDateTime.now();
    }

    // =========================
    // Relations with cascading
    // =========================
    @OneToMany(mappedBy = "alert", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<AlertDeviceMapping> deviceMappings = new ArrayList<>();

    @OneToMany(mappedBy = "alert", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<AlertGeofenceMapping> geofenceMappings = new ArrayList<>();

    @OneToMany(mappedBy = "alert", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<AlertRouteMapping> routeMappings = new ArrayList<>();
    
    @OneToOne(mappedBy = "alert",cascade = CascadeType.ALL,orphanRemoval = true,fetch = FetchType.LAZY)
    private AlertDetails alertDetails;
    
    @OneToMany(mappedBy = "alert", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<AlertPoiMapping> poiMappings = new ArrayList<>();
    
    @OneToOne(mappedBy = "alert", cascade = CascadeType.ALL, orphanRemoval = true)
    private AlertDeviceCommandMapping deviceCommand;
    
    @OneToMany(mappedBy = "alert", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<AlertSchedule> schedules = new ArrayList<>();
    
    @OneToMany(mappedBy = "alert", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<AlertNotification> notifications = new ArrayList<>();
    
    @OneToMany(mappedBy = "alert", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<AlertUserMapping> alertUsers = new ArrayList<>();

    public void addAlertUser(AlertUserMapping u) {
        alertUsers.add(u);
        u.setAlert(this);
    }


    public void addNotification(AlertNotification n) {
        notifications.add(n);
        n.setAlert(this);
    }


    public void addSchedule(AlertSchedule s) {
        schedules.add(s);
        s.setAlert(this);
    }


    public void setDeviceCommand(AlertDeviceCommandMapping cmd) {
        if (cmd != null) {
            cmd.setAlert(this);
        }
        this.deviceCommand = cmd;
    }


    public void addPoiMapping(AlertPoiMapping poi) {
        poiMappings.add(poi);
        poi.setAlert(this);
    }

    public void removePoiMapping(AlertPoiMapping poi) {
        poiMappings.remove(poi);
        poi.setAlert(null);
    }

    
    public void addDeviceMapping(AlertDeviceMapping mapping) {
        if (!deviceMappings.contains(mapping)) {
            deviceMappings.add(mapping);
            mapping.setAlert(this);
        }
    }

    public void removeDeviceMapping(AlertDeviceMapping mapping) {
        deviceMappings.remove(mapping);
        mapping.setAlert(null);
    }

    public void addGeofenceMapping(AlertGeofenceMapping mapping) {
        if (!geofenceMappings.contains(mapping)) {
            geofenceMappings.add(mapping);
            mapping.setAlert(this);
        }
    }

    public void removeGeofenceMapping(AlertGeofenceMapping mapping) {
        geofenceMappings.remove(mapping);
        mapping.setAlert(null);
    }

    public void addRouteMapping(AlertRouteMapping mapping) {
        if (!routeMappings.contains(mapping)) {
            routeMappings.add(mapping);
            mapping.setAlert(this);
        }
    }

    public void removeRouteMapping(AlertRouteMapping mapping) {
        routeMappings.remove(mapping);
        mapping.setAlert(null);
    }
    
    public void setAlertDetails(AlertDetails details) {
        if (details != null) {
            details.setAlert(this);
        }
        this.alertDetails = details;
    }
}
