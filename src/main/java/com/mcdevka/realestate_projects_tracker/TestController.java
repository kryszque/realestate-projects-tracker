package com.mcdevka.realestate_projects_tracker;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

// Mówi Springowi, że ta klasa będzie obsługiwać żądania HTTP
@RestController
public class TestController {

    // Mapuje żądania GET wysłane na adres "/hello" do tej metody
    @GetMapping("/hello")
    public String sayHello() {
        return "Witaj w Project Tracker API!";
    }
}