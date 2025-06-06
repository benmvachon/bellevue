package com.village.bellevue.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class ReactController {

  @RequestMapping(value = {"/{path:[^\\.]*}", "/{path:^(?!api|ws).*}/{id:[^\\.]*}"})
  public String redirect(HttpServletRequest request) {
    String uri = request.getRequestURI();
    
    // Skip backend-specific paths
    if (uri.startsWith("/api") || uri.startsWith("/ws") || uri.startsWith("/actuator")) {
      return null; // Let Spring handle it
    }

    System.out.println("ReactController handling path: " + uri);
    return "forward:/index.html";
  }
}
