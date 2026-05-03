package com.trackingpath.controllers;

import com.trackingpath.entities.Menu;
import com.trackingpath.entities.Users;
import com.trackingpath.repositories.UserRepository;
import com.trackingpath.services.AuthenticationService;
import com.trackingpath.services.MenuService;

import jakarta.servlet.http.HttpServletRequest;

import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api")
public class MenuController {

	@Autowired
	private MenuService menuService;
	@Autowired
	private UserRepository usersRepository;

	@Autowired
	AuthenticationService authenticationService;

	@GetMapping("/menu")
	public ResponseEntity<?> getMenu(Principal principal) {

		String username = principal.getName(); // to get only username
		Users user = authenticationService.getCurrentUser();// get users all details;
       // List<String> roles = authenticationService.getRoleNames(); //get the user's roles LIST

      // System.out.println("ROLES = " + roles.get(0).toString());
        
		List<Menu> menuList = menuService.getMenuForUser(user.getUsername());

		return ResponseEntity.ok(menuList);
	}

}
