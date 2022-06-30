package com.vi.appointmentservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

public abstract class CalComService {
    RestTemplate restTemplate;
    String calcomApiUrl;
    String calcomApiKey;

    ObjectMapper objectMapper;

    public CalComService(RestTemplate restTemplate, String calcomApiUrl, String calcomApiKey) {
        this.restTemplate = restTemplate;
        this.calcomApiUrl = calcomApiUrl;
        this.calcomApiKey = calcomApiKey;
        this.objectMapper = new ObjectMapper();
    }

    String buildUri(String path) {
        return UriComponentsBuilder.newInstance().scheme("https").host(calcomApiUrl).path(path).queryParam("apiKey", calcomApiKey).build().toUriString();
    }
}
