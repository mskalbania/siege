package com.reactive.siege.controller;

import com.reactive.siege.model.SiegeRequest;
import com.reactive.siege.service.SiegeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/siege")
public class SiegeController {

    private final SiegeService siegeService;

    public SiegeController(SiegeService siegeService) {
        this.siegeService = siegeService;
    }

    @PostMapping("/start")
    public ResponseEntity<String> start(@RequestBody SiegeRequest siegeRequest) {
        boolean started = siegeService.start(siegeRequest.getUrl(), siegeRequest.getTps());
        if (started) {
            return ResponseEntity.ok("Siege started...");
        } else {
            return ResponseEntity.badRequest().body("Already running...");
        }
    }

    @GetMapping("/stop")
    public ResponseEntity<?> stop() {
        return siegeService.stop()
                .map(ResponseEntity::ok)
                .getOrElse(ResponseEntity.badRequest().build());
    }
}
