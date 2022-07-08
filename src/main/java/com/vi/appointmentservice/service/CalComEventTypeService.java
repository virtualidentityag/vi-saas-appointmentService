package com.vi.appointmentservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vi.appointmentservice.api.model.CalcomEventType;
import com.vi.appointmentservice.api.model.CalcomEventType;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CalComEventTypeService extends CalComService {

    public CalComEventTypeService(@NonNull RestTemplate restTemplate, @Value("${calcom.apiUrl}") String calcomApiUrl, @Value("${calcom.apiKey}") String calcomApiKey, @NonNull ObjectMapper objectMapper) {
        super(restTemplate, calcomApiUrl, calcomApiKey);
    }

    public String generateSlug(String name) {
        name = name.toLowerCase();
        String regex = "[^\\w]+"; // Relace everyting but word characters (digits, numbers)
        String subst = "-";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(name);
        return matcher.replaceAll(subst);
    }

    public List<CalcomEventType> getAllEventTypes() throws JsonProcessingException {
        String response = this.restTemplate.getForObject(this.buildUri("/v1/event-types"), String.class);
        JSONObject jsonObject = new JSONObject(response);
        response = jsonObject.getJSONArray("event_types").toString();
        ObjectMapper mapper = new ObjectMapper();
        List<CalcomEventType> result = mapper.readValue(response, new TypeReference<List<CalcomEventType>>(){});
        return result;
    }

    public List<CalcomEventType> getAllEventTypesOfTeam(Long teamId) throws JsonProcessingException {
        String response = this.restTemplate.getForObject(this.buildUri("/v1/event-types"), String.class);
        JSONObject jsonObject = new JSONObject(response);
        response = jsonObject.getJSONArray("event_types").toString();
        ObjectMapper mapper = new ObjectMapper();
        List<CalcomEventType> result = mapper.readValue(response, new TypeReference<List<CalcomEventType>>(){});
        // TODO: Add correct filter: .filter(eventType -> eventType.getTeamId() != null && eventType.getTeamId() == teamId.intValue())
        return new ArrayList<>(result).stream()
                .filter(eventType -> eventType.getTeamId() != null)
                .collect(Collectors.toList());
    }

    public List<CalcomEventType> getAllEventTypesOfUser(Long userId) throws JsonProcessingException {
        String response = this.restTemplate.getForObject(this.buildUri("/v1/event-types"), String.class);
        JSONObject jsonObject = new JSONObject(response);
        response = jsonObject.getJSONArray("event_types").toString();
        ObjectMapper mapper = new ObjectMapper();
        List<CalcomEventType> result = mapper.readValue(response, new TypeReference<List<CalcomEventType>>(){});
        // TODO: add correct filter .filter(eventType -> eventType.getUserId() != null && eventType.getUserId() == userId.intValue())
        return new ArrayList<>(result).stream()
                .filter(eventType -> eventType.getUserId() != null)
                .collect(Collectors.toList());
    }

    public CalcomEventType getEventTypeById(Long eventTypeId) throws JsonProcessingException {
        String response = this.restTemplate.getForObject(this.buildUri("/v1/event-types"), String.class);
        JSONObject jsonObject = new JSONObject(response);
        response = jsonObject.getJSONArray("event_types").toString();
        ObjectMapper mapper = new ObjectMapper();
        List<CalcomEventType> result = mapper.readValue(response, new TypeReference<List<CalcomEventType>>(){});
        return new ArrayList<>(result).stream()
                .filter(eventType -> eventType.getId() == eventTypeId.intValue())
                .collect(Collectors.toList()).get(0);
    }

    public CalcomEventType createEventType(JSONObject eventType) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(eventType.toString(), headers);
        return restTemplate.postForEntity(this.buildUri("/v1/event-types"), request, CalcomEventType.class).getBody();
    }

    public CalcomEventType editEventType(JSONObject eventType) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(eventType.toString(), headers);
        return restTemplate.postForEntity(this.buildUri("/v1/event-types/" + eventType.get("id")), request, CalcomEventType.class).getBody();
    }

    public HttpStatus deleteEventType(Long eventTypeId) {
        return restTemplate.exchange(this.buildUri("/v1/event-types/" + eventTypeId), HttpMethod.DELETE, null, String.class).getStatusCode();
    }


}