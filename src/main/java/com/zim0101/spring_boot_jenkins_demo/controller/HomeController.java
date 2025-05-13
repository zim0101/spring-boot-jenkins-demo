package com.zim0101.spring_boot_jenkins_demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class HomeController {

    @GetMapping("/")
    public ResponseEntity<Map<String, String>> index() {
        Map<String, String> response = new HashMap<>();

        response.put("status", "success");
        response.put("code", "200");
        response.put("version", "v1");
        response.put("message", "Welcome to Spring Boot with Jenkins Application!");

        return ResponseEntity.ok(response);
    }
}
