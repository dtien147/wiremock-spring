package com.example.demo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class ProxyController {

    private final RestTemplate restTemplate = new RestTemplate();

    @GetMapping("/api/endpoint")
    public String getMockedResponse() {
        // Forward the request to WireMock running on port 8081
        return restTemplate.getForObject("http://localhost:8081/api/endpoint", String.class);
    }
}
