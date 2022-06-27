package com.vi.appointmentservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vi.appointmentservice.api.model.CalcomUser;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Objects;

@Service
@Slf4j
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

    @SneakyThrows
    public List<CalcomUser> getUsers() {
        // ResponseEntity<CalcomUser[]> response = restTemplate.getForEntity(String.format(this.buildUri("/users"), calcomApiUrl, calcomApiKey), CalcomUser[].class);

        String response = restTemplate.getForObject(String.format(this.buildUri("/v1/users"), calcomApiUrl, calcomApiKey), String.class);
        JSONObject jsonObject = new JSONObject(response);
        log.debug(String.valueOf(jsonObject));
        response = jsonObject.getJSONArray("users").toString();
        log.debug(response);
        ObjectMapper mapper = new ObjectMapper();
        CalcomUser[] result = mapper.readValue(response, CalcomUser[].class);

        return List.of(Objects.requireNonNull(result));
    }

    @SneakyThrows
    public CalcomUser getUserById(Long userId) {
        String response = restTemplate.getForObject(String.format(this.buildUri("/v1/users/"+userId), calcomApiUrl, calcomApiKey), String.class);
        JSONObject jsonObject = new JSONObject(response);
        log.debug(String.valueOf(jsonObject));
        response = jsonObject.getJSONObject("user").toString();
        log.debug(response);
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(response, CalcomUser.class);
    }

}
