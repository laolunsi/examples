package com.example.demo.controller.common;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author zfh
 * @version 1.0
 * @since 2019/4/3 9:01
 */
@Controller
public class BasicAction {

    @GetMapping(value = "/")
    public ModelAndView index() {
        return new ModelAndView("index");
    }

    @GetMapping(value = "chat")
    public ModelAndView setUser() {
        return new ModelAndView("chat");
    }

}
