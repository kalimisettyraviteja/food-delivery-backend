package com.fooddelivery.restaurantservice.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
public class PingController {

    @GetMapping("/ping")
    public String ping() {
        return "OK";
    }
}