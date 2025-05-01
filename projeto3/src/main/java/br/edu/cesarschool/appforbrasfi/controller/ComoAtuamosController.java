package br.edu.cesarschool.appforbrasfi.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ComoAtuamosController
{
    @GetMapping("/hubsDeNetworking")
    public ModelAndView hubsDeNetworking()
    {
        ModelAndView mv = new ModelAndView();
        mv.setViewName("ComoAtuamos/hubsDeNetworking");
        return mv;
    }

}
