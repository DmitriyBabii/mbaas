package com.mbaas.controllers;

import com.backendless.Backendless;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

@RestController
@RequestMapping("/report")
@AllArgsConstructor
public class ReportController {
    @GetMapping
    public ModelAndView getPlaces(ModelAndView modelAndView) {
        modelAndView.setViewName("report.html");
        return modelAndView;
    }

    @PostMapping
    public ModelAndView getPlaces(@RequestParam("report") String report) {
        Backendless.Messaging.sendTextEmail("Report", report, "v.babiy75@gmail.com");
        return new ModelAndView("redirect:/");
    }
}
