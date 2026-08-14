package com.sidhant.ticket_management.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/support")
public class SupportTestController {

    @GetMapping("/test")
    public String test() {
        return "Support Engineer access granted";
    }
}