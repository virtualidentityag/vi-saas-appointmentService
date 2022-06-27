package com.vi.appointmentservice.service;

import com.vi.appointmentservice.api.model.CalcomUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class CalComService {

    private RestTemplate restTemplate;
    private String calcomApiUrl;
    private String calcomApiKey;

    @Autowired
    public CalComService(RestTemplate restTemplate, @Value("${calcom.apiUrl}") String calcomApiUrl, @Value("${calcom.apiKey}") String calcomApiKey) {
        this.restTemplate = restTemplate;
        this.calcomApiUrl = calcomApiUrl;
        this.calcomApiKey = calcomApiKey;
    }

    private String buildUri(String path){
        return UriComponentsBuilder.newInstance().scheme("https").host(calcomApiUrl).path(path).queryParam("apiKey", calcomApiKey).build().toUriString();
    }

    public List<CalcomUser> getUsers(){
        ResponseEntity<CalcomUser[]> response = restTemplate.getForEntity(String.format(this.buildUri("/users"), calcomApiUrl, calcomApiKey), CalcomUser[].class);
        return List.of(Objects.requireNonNull(response.getBody()));
    }

}
