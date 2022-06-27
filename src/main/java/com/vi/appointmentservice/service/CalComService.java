package com.vi.appointmentservice.service;

import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

public abstract class CalComService {
    RestTemplate restTemplate;
    String calcomApiUrl;
    String calcomApiKey;

    public CalComService(RestTemplate restTemplate, String calcomApiUrl, String calcomApiKey) {
        this.restTemplate = restTemplate;
        this.calcomApiUrl = calcomApiUrl;
        this.calcomApiKey = calcomApiKey;
    }

    String buildUri(String path) {
        return UriComponentsBuilder.newInstance().scheme("https").host(calcomApiUrl).path(path).queryParam("apiKey", calcomApiKey).build().toUriString();
    }
}
