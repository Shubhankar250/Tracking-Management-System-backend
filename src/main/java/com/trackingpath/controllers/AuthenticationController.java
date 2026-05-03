// src/main/java/com/trackingpath/controllers/AuthenticationController.java
package com.trackingpath.controllers;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.velocity.VelocityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.trackingpath.services.JwtService;
import com.trackingpath.services.UserService;
import com.trackingpath.services.ZlmService;
import com.trackingpath.util.EncDcryptUtil;
import com.trackingpath.util.MailUtil;
import com.trackingpath.util.VelocityEngineUtil;

import jakarta.servlet.http.HttpServletRequest;

import com.trackingpath.services.ActivityLogService;
import com.trackingpath.services.AuthenticationService;
import com.trackingpath.entities.*;
import com.trackingpath.repositories.UserRepository;
import com.trackingpath.responses.LoginResponse;
import com.trackingpath.dtos.CustomUserDTO;
import com.trackingpath.dtos.ExternalAccessTokenDTO;
import com.trackingpath.dtos.LoginUserDto;
import com.trackingpath.dtos.MessageDTO;
import com.trackingpath.dtos.RegisterUserDto;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RequestMapping("/auth")
@RestController
public class AuthenticationController {
	private final JwtService jwtService;
	private final AuthenticationService authenticationService;
	private final ZlmService zlmService;
	@Autowired
	UserRepository userRepository;
	@Autowired
	private UserService userService;
	@Autowired
	private ActivityLogService activityLogService;
	@Value("${BaseURL}")
	private String BaseURL;

	public AuthenticationController(JwtService jwtService, AuthenticationService authenticationService, ZlmService zlmService) {
		this.jwtService = jwtService;
		this.authenticationService = authenticationService;
		this.zlmService=zlmService;
	}

	@PostMapping("/signup")
	public ResponseEntity<Users> register(@RequestBody RegisterUserDto registerUserDto) {
		Users registeredUser = authenticationService.signup(registerUserDto);
		return ResponseEntity.ok(registeredUser);
	}

	@PostMapping("/login")
	public ResponseEntity<LoginResponse> authenticate(@RequestBody LoginUserDto loginUserDto,
			HttpServletRequest request) {

		Users authenticatedUser = authenticationService.authenticate(loginUserDto);

	    authenticatedUser.setLoggedIn(LocalDateTime.now());
	    authenticatedUser.setLoggedOut(null);
	    userRepository.save(authenticatedUser);
		String jwtToken = jwtService.generateToken(authenticatedUser);
		activityLogService.createActivity("USER LOGIN", "USER(" + authenticatedUser.getUsername() + ") LOGOIN",
				authenticatedUser, request);
		ExternalAccessTokenDTO externalToken = zlmService.getTokenByProject(); 
		List<String> roles = authenticatedUser.getRoles().stream().map(Role::getRoleName).toList();
		LoginResponse loginResponse = new LoginResponse();
		loginResponse.setId(authenticatedUser.getId());
		loginResponse.setUsername(authenticatedUser.getUsername());
		loginResponse.setToken(jwtToken);
		loginResponse.setExpiresIn(jwtService.getExpirationTime());
		loginResponse.setRoles(roles);
		 if (externalToken != null) {
             loginResponse.setZlm_token(externalToken.getExternalAccessToken());
             loginResponse.setUrl(externalToken.getUrl());
     } else {
             loginResponse.setZlm_token(null);
             loginResponse.setUrl(null);
     }

		
		return ResponseEntity.ok(loginResponse);
	}

	@PostMapping("/resetPassword")
	public ResponseEntity<String> forgotPassword(@RequestParam String username, @RequestParam String email) {

		if (email == null || email.isEmpty()) {
			return ResponseEntity.badRequest().body("Enter valid email address!");
		}

		if (username == null || username.isEmpty()) {
			return ResponseEntity.badRequest().body("Enter valid username!");
		}

		String token = RandomStringUtils.random(30, true, true);
		boolean status = userService.updateToken(username, token);

		if (!status) {
			return ResponseEntity.badRequest().body("Invalid username!");
		}

		// Send Email
		VelocityContext context = new VelocityContext();
		context.put("username", username);
		context.put("resetlink", BaseURL + "/users_verfication?token=" + token);

		MessageDTO msg = new MessageDTO();
		msg.setMessageTitle("Please verify your account!");
		msg.setMessageBody(VelocityEngineUtil.getHtmlText(context, "forgot_pwd_template.vm"));

		MailUtil.send(email, null, msg, null);

		return ResponseEntity.ok("We have e-mailed your password reset link!");
	}

	// ================= PASSWORD SUBMIT =================
	@PostMapping("/users_verfication")
	public String submitForm(@RequestParam("token") String token,
			@RequestParam("confirm_password") String confirm_password,
			@RequestParam("new_password") String new_password, HttpServletRequest request,
			final RedirectAttributes redirectAttributes) {

		if (!new_password.equals(confirm_password)) {
			redirectAttributes.addFlashAttribute("msg", "Password does not match!");
			return "redirect:/";
		}

		String hashedPassword = EncDcryptUtil.generateHash(new_password);

		boolean status = userService.newPasswordReset(hashedPassword, token);
		Users user = authenticationService.getUser();
		if (status) {
			userService.updateDisableStatus(token);
			redirectAttributes.addFlashAttribute("msg", "Password changed successfully!");
			activityLogService.createActivity("PASSWORD CHANGED",
					"USER(" + user.getUsername() + ") PASSWORD CHANGED BY " + user.getUsername(), user, request);
		} else {
			redirectAttributes.addFlashAttribute("msg", "Invalid or expired token!");
		}

		return "redirect:/";
	}

	@GetMapping("/linkexpire")
	public ResponseEntity<Map<String, Object>> updateEnableStatus(@RequestParam("token") String token) {
		boolean status = userService.getEnableStatus(token);

		Map<String, Object> response = new HashMap<>();
		response.put("status", status);

		if (!status) {
			response.put("msg", "Your password reset link has expired!");
		} else {
			response.put("msg", "Link is valid."); // optional
		}

		return ResponseEntity.ok(response);
	}
}
