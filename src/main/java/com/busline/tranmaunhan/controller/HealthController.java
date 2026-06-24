package com.busline.tranmaunhan.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HealthController {

    @GetMapping({"/health", "/api/health"})
    public Map<String, Boolean> health() {
        return Map.of("ok", true);
    }
}
