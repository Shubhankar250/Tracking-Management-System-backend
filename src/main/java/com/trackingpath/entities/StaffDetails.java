package com.trackingpath.entities;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "staff_details")
@Data
public class StaffDetails {
	  @Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    private Long id;

	    @Column(name = "name", nullable = false)
	    private String name;

	    @Column(name = "designation")
	    private String designation;

	    @Column(name = "email", unique = true)
	    private String email;

	    @Column(name = "employee_code", unique = true)
	    private String employeeCode;

	    @Column(name = "mobile_number")
	    private String mobileNumber;

	    @Column(name = "user_id")
	    private Long userId;
}
