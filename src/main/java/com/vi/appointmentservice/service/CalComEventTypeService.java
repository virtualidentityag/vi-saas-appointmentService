package com.vi.appointmentservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vi.appointmentservice.api.model.CalcomEventType;
import com.vi.appointmentservice.api.model.CalcomTeam;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class CalComEventTypeService extends CalComService {
    @Autowired
    public CalComEventTypeService(RestTemplate restTemplate, @Value("${calcom.apiUrl}") String calcomApiUrl, @Value("${calcom.apiKey}") String calcomApiKey) {
        super(restTemplate, calcomApiUrl, calcomApiKey);
    }

    public String generateSlug(String name){
        name = name.toLowerCase();
        String regex = "[^\\w]+"; // Relace everyting but word characters (digits, numbers)
        String subst = "-";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(name);
        return matcher.replaceAll(subst);
    }

    public List<CalcomEventType> getAllEventTypes() throws JsonProcessingException {
        String response = this.restTemplate.getForObject(String.format(this.buildUri("/v1/event-types"), calcomApiUrl, calcomApiKey), String.class);
        JSONObject jsonObject = new JSONObject(response);
        response = jsonObject.getJSONArray("teams").toString();
        ObjectMapper mapper = new ObjectMapper();
        CalcomEventType[] result = mapper.readValue(response, CalcomEventType[].class);
        return List.of(Objects.requireNonNull(result));
    }

    public CalcomEventType getEventTypeById(Long eventTypeId) throws JsonProcessingException {
        String response = restTemplate.getForObject(String.format(this.buildUri("/v1/event-types/" + eventTypeId), calcomApiUrl, calcomApiKey), String.class);
        JSONObject jsonObject = new JSONObject(response);
        response = jsonObject.getJSONObject("event_type").toString();
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(response, CalcomEventType.class);
    }

    public CalcomEventType createEventType(JSONObject eventType){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(eventType.toString(), headers);
        return restTemplate.postForEntity(this.buildUri("/v1/event-types"), request , CalcomEventType.class ).getBody();
    }

    public CalcomEventType editTeam(JSONObject eventType){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(eventType.toString(), headers);
        return restTemplate.postForEntity(this.buildUri("/v1/event-types/"+eventType.get("id")), request , CalcomEventType.class ).getBody();
    }

}