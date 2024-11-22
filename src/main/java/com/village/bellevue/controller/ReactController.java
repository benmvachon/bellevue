package com.village.bellevue.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ReactController {

  @RequestMapping(value = {"/{path:[^\\.]*}", "/{path:^(?!api).*}/{id:[^\\.]*}"})
  public String redirect(HttpServletRequest request) {
    System.out.println("ReactController handling path: " + request.getRequestURI());
    return "forward:/index.html";
  }
}
