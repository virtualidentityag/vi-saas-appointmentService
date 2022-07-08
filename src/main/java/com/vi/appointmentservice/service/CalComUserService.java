package com.vi.appointmentservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vi.appointmentservice.api.model.CalcomUser;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class CalComUserService extends CalComService {

    @Autowired
    public CalComUserService(RestTemplate restTemplate, @Value("${calcom.apiUrl}") String calcomApiUrl, @Value("${calcom.apiKey}") String calcomApiKey) {
        super(restTemplate, calcomApiUrl, calcomApiKey);
    }

    // Users
    public List<CalcomUser> getUsers() throws JsonProcessingException {
        // ResponseEntity<CalcomUser[]> response = restTemplate.getForEntity(String.format(this.buildUri("/users"), calcomApiUrl, calcomApiKey), CalcomUser[].class);
        String response = this.restTemplate.getForObject(this.buildUri("/v1/users"), String.class);
        JSONObject jsonObject = new JSONObject(response);
        response = jsonObject.getJSONArray("users").toString();
        ObjectMapper mapper = new ObjectMapper();
        CalcomUser[] result = mapper.readValue(response, CalcomUser[].class);
        return List.of(Objects.requireNonNull(result));
    }


    public CalcomUser createUser(JSONObject user) throws JsonProcessingException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        log.info("Creating calcom user: {}", user);
        HttpEntity<String> request = new HttpEntity<>(user.toString(), headers);
        ResponseEntity<String> response = restTemplate.postForEntity(this.buildUri("/v1/users"), request, String.class);
        String body = response.getBody();
        if(response.getStatusCode() == HttpStatus.OK){
            JSONObject jsonObject = new JSONObject(body);
            body = jsonObject.getJSONArray("users").toString();
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(body, CalcomUser.class);
        } else {
            return null;
        }
    }

    public CalcomUser updateUser(JSONObject user) throws JsonProcessingException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        log.info("Creating calcom user: {}", user);
        HttpEntity<String> request = new HttpEntity<>(user.toString(), headers);
        String response = restTemplate.postForEntity(this.buildUri("/v1/users" + user.getLong("id")), request, String.class).getBody();
        JSONObject jsonObject = new JSONObject(response);
        response = jsonObject.getJSONArray("users").toString();
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(response, CalcomUser.class);
    }

    public HttpStatus deleteUser(Long userId) {
        return restTemplate.exchange(this.buildUri("/v1/users/" + userId), HttpMethod.DELETE, null, String.class).getStatusCode();
    }


    public CalcomUser getUserById(Long userId) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        ResponseEntity<java.lang.String> response = restTemplate.exchange(this.buildUri("/v1/users/" + userId), HttpMethod.GET, null, String.class);
        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null){
            JSONObject jsonObject = new JSONObject(response.getBody());
            if(jsonObject.getJSONObject("user") != null){
                return mapper.readValue(jsonObject.getJSONObject("user").toString(), CalcomUser.class);
            }
        }
        return null;
    }
}
