package com.fooddelivery.orderservice.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "https://render-ping.vercel.app/home")
public class PingController {

    @GetMapping("/ping")
    public String ping() {
        return "OK";
    }
}