package com.trackingpath.entities;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.*;
import lombok.Data;
@Entity
@Data
@Table(name = "menu")
public class Menu {

	  @Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    private Long id;

	    private String menuName;

	    private String url;

	    @ManyToOne
	    @JoinColumn(name = "role_id")
	    private Role role;

	    @ManyToOne
	    @JoinColumn(name = "parent_id")
	    @JsonBackReference
	    private Menu parent;

	    @OneToMany(mappedBy = "parent")
	    @JsonManagedReference
	    private List<Menu> children = new ArrayList<>();

	    @Column(name = "icon_name")
	    private String iconName;

}

