package com.village.bellevue.controller;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class ReactController implements ErrorController {

  @RequestMapping(value = {"/{path:[^\\.]*}", "/{path:^(?!api|ws).*}/{id:[^\\.]*}"})
  public String redirect(HttpServletRequest request) {
    String uri = request.getRequestURI();

    if (uri.startsWith("/api") || uri.startsWith("/ws") || uri.startsWith("/actuator")) {
      return null;
    }

    System.out.println("ReactController handling path: " + uri);
    return "forward:/index.html";
  }
}
