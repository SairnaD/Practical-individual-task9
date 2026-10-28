package bootcamp.hibernate_practical.controller;

import bootcamp.hibernate_practical.client.ExternalApiClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/external")
public class ExternalApiController {
    private final ExternalApiClient externalApiClient;

    public ExternalApiController(ExternalApiClient externalApiClient) {
        this.externalApiClient = externalApiClient;
    }

    @GetMapping("/status")
    public Map<String, Object> status() {
        return externalApiClient.checkStatus();
    }
}