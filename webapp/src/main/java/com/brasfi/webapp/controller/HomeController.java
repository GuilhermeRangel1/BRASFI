package com.brasfi.webapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class HomeController {

    @GetMapping("/")
    public ModelAndView home() {
        ModelAndView mv = new ModelAndView("inicio");
        return mv;
    }

    @GetMapping("/sobre")
    public ModelAndView sobre() {
        return new ModelAndView("sobre");
    }

}

