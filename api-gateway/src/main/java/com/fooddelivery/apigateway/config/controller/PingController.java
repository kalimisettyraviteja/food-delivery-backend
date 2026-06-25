package com.fooddelivery.apigateway.config.controller;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PingController {

    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        System.out.println("PING HIT at " + java.time.LocalDateTime.now());
        return ResponseEntity.ok("OK");
    }
}