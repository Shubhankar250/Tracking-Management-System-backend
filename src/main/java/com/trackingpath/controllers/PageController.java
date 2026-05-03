package com.trackingpath.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {
  @GetMapping({"/","/login"}) public String login() { return "login"; }       // -> /WEB-INF/jsp/login.jsp
  @GetMapping("/dashboard")  public String dash()  { return "dashboard"; }    // -> /WEB-INF/jsp/dashboard.jsp
}
