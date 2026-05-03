package com.trackingpath.services;

import com.trackingpath.entities.Menu;
import com.trackingpath.entities.Role;
import com.trackingpath.entities.Users;
import com.trackingpath.repositories.MenuRepository;
import com.trackingpath.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class MenuService {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private MenuRepository menuRepository;

	public List<Menu> getMenuForUser(String username) {

	    Users user = userRepository.findByUsername(username);
	    if (user == null) {
	        throw new RuntimeException("User not found: " + username);
	    }

	    // 1️⃣ Collect role IDs
	    List<Long> roleIds = user.getRoles().stream().map(Role::getId).toList();

	    if (roleIds.isEmpty()) {
	        return List.of();
	    }

	    // 2️⃣ Fetch parent menus for ALL roles
	    List<Menu> parents = menuRepository
	            .findByParentIsNullAndRole_IdIn(roleIds);

	    // 3️⃣ For each parent, fetch children for SAME roles
	    for (Menu parent : parents) {

	        List<Menu> children = menuRepository
	                .findByParent_IdAndRole_IdIn(parent.getId(), roleIds);

	        // prevent infinite recursion
	        for (Menu c : children) {
	            c.setParent(null);
	            c.setChildren(null);
	        }

	        parent.setParent(null);
	        parent.setChildren(children);
	    }

	    return parents;
	}



}
