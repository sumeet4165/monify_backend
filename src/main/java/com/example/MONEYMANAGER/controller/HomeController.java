package com.example.MONEYMANAGER.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/status")
public class HomeController {

    // Check if application is running
    @GetMapping
    public String healthcheck() {
        return "App is runnning";
    }
}
