package com.vi.appointmentservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vi.appointmentservice.api.model.CalcomUser;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
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

        String response = this.restTemplate.getForObject(String.format(this.buildUri("/v1/users"), calcomApiUrl, calcomApiKey), String.class);
        JSONObject jsonObject = new JSONObject(response);
        log.debug(String.valueOf(jsonObject));
        response = jsonObject.getJSONArray("users").toString();
        log.debug(response);
        ObjectMapper mapper = new ObjectMapper();
        CalcomUser[] result = mapper.readValue(response, CalcomUser[].class);

        return List.of(Objects.requireNonNull(result));
    }


    public CalcomUser createUser(CalcomUser user) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        // MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        MultiValueMap<String, String> map = objectMapper.convertValue(user, MultiValueMap.class);
        JSONObject userObject = new JSONObject(user);
        log.debug("Creating calcom user: {}", userObject);
        HttpEntity<String> request = new HttpEntity<>(userObject.toString(), headers);
        return restTemplate.postForEntity(this.buildUri("/v1/user"), request, CalcomUser.class).getBody();
    }

    public CalcomUser updateUser(CalcomUser user) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        // MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        MultiValueMap<String, String> map = objectMapper.convertValue(user, MultiValueMap.class);
        JSONObject userObject = new JSONObject(user);
        log.debug("Updating calcom user: {}", userObject);
        HttpEntity<String> request = new HttpEntity<>(userObject.toString(), headers);
        return restTemplate.postForEntity(this.buildUri("/v1/user/" + user.getId()), request, CalcomUser.class).getBody();
    }


    public CalcomUser getUserById(Long userId) throws JsonProcessingException {
        String response = restTemplate.getForObject(String.format(this.buildUri("/v1/users/" + userId), calcomApiUrl, calcomApiKey), String.class);
        JSONObject jsonObject = new JSONObject(response);
        log.debug(String.valueOf(jsonObject));
        response = jsonObject.getJSONObject("user").toString();
        log.debug(response);
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(response, CalcomUser.class);
    }
}
