package com.vi.appointmentservice.api.service.calcom;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vi.appointmentservice.api.exception.httpresponses.BadRequestException;
import com.vi.appointmentservice.api.exception.httpresponses.CalComApiErrorException;
import com.vi.appointmentservice.api.model.CalcomEventTypeDTO;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class CalComEventTypeService extends CalComService {

  public CalComEventTypeService(@NonNull RestTemplate restTemplate,
      @Value("${calcom.apiUrl}") String calcomApiUrl,
      @Value("${calcom.apiKey}") String calcomApiKey, @NonNull ObjectMapper objectMapper) {
    super(restTemplate, calcomApiUrl, calcomApiKey);
  }

  public List<CalcomEventTypeDTO> getAllEventTypes() {
    String response = this.restTemplate.getForObject(this.buildUri("/v1/event-types"),
        String.class);
    JSONObject jsonObject;
    if (response != null) {
      jsonObject = new JSONObject(response);
    } else {
      throw new CalComApiErrorException("Calcom event-type API response was null");
    }
    response = jsonObject.getJSONArray("event_types").toString();
    ObjectMapper mapper = new ObjectMapper();
    try {
      return mapper.readValue(response, new TypeReference<>() {
      });
    } catch (JsonProcessingException e) {
      throw new CalComApiErrorException("Could not deserialize event-types response from calcom api");
    }

  }

  public void deleteAllEventTypesOfUser(Long userId) {
    List<CalcomEventTypeDTO> eventTypesOfUser = new ArrayList<>(this.getAllEventTypes()).stream()
        .filter(eventType -> eventType.getUserId() != null
            && eventType.getUserId() == userId.intValue())
        .collect(Collectors.toList());
    for (CalcomEventTypeDTO eventType : eventTypesOfUser) {
      this.deleteEventType(Long.valueOf(eventType.getId()));
    }
  }

  public List<CalcomEventTypeDTO> getAllEventTypesOfTeam(Long teamId) {
    List<CalcomEventTypeDTO> result = this.getAllEventTypes();
    return new ArrayList<>(result).stream()
        .filter(eventType -> eventType.getTeamId() != null && eventType.getTeamId() == teamId.intValue())
        .collect(Collectors.toList());
  }

  public List<CalcomEventTypeDTO> getAllEventTypesOfUser(Long userId) {
    List<CalcomEventTypeDTO> result = this.getAllEventTypes();
    return new ArrayList<>(result).stream()
        .filter(eventType -> eventType.getUserId() != null
            && eventType.getUserId() == userId.intValue())
        .collect(Collectors.toList());
  }

  public CalcomEventTypeDTO getEventTypeById(Long eventTypeId) {
    List<CalcomEventTypeDTO> result = this.getAllEventTypes();
    CalcomEventTypeDTO found = new ArrayList<>(result).stream()
        .filter(eventType -> eventType.getId() == eventTypeId.intValue())
        .collect(Collectors.toList()).get(0);
    if (found != null) {
      return found;
    } else {
      throw new BadRequestException(
          String.format("No calcom event-type found for id '%s'", eventTypeId));
    }
  }

  public CalcomEventTypeDTO createEventType(JSONObject eventType) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<String> request = new HttpEntity<>(eventType.toString(), headers);
    try {
      ResponseEntity<String> response = restTemplate
          .exchange(this.buildUri("/v1/event-types"), HttpMethod.POST, request,
              String.class);
      String body = response.getBody();
      if (body == null) {
        throw new CalComApiErrorException("Calcom create event-type API response body was null");
      }
      JSONObject jsonObject = new JSONObject(body);
      body = jsonObject.getJSONObject("event_type").toString();
      ObjectMapper mapper = new ObjectMapper();
      mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
      return mapper.readValue(body, CalcomEventTypeDTO.class);
    } catch (Exception e) {
      log.error("Calcom create event-type API response exception", e);
      throw new CalComApiErrorException("Calcom create event-type API response exception");
    }
  }

  public CalcomEventTypeDTO editEventType(Long eventTypeId, JSONObject eventType) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<String> request = new HttpEntity<>(eventType.toString(), headers);
    try {
      ResponseEntity<String> response = restTemplate
          .exchange(this.buildUri("/v1/event-types/" + eventTypeId), HttpMethod.PATCH, request,
              String.class);
      String body = response.getBody();
      if (body == null) {
        throw new CalComApiErrorException("Calcom update event-type API response body was null");
      }
      JSONObject jsonObject = new JSONObject(body);
      body = jsonObject.getJSONObject("event_type").toString();
      ObjectMapper mapper = new ObjectMapper();
      mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
      return mapper.readValue(body, CalcomEventTypeDTO.class);
    } catch (Exception e) {
      log.error("Calcom update event-type API response exception", e);
      throw new CalComApiErrorException("Calcom update event-type API response exception");
    }
  }

  public HttpStatus deleteEventType(Long eventTypeId) {
    return restTemplate.exchange(this.buildUri("/v1/event-types/" + eventTypeId), HttpMethod.DELETE,
        null, String.class).getStatusCode();
  }

  public CalcomEventTypeDTO getDefaultEventTypeOfTeam(Long teamId) {
    List<CalcomEventTypeDTO> allEventTypesOfTeam = getAllEventTypesOfTeam(teamId);
    return allEventTypesOfTeam.stream().filter(el -> {
      try {
        String metadata = (String) el.getMetadata();
        JSONObject jsonObject = new JSONObject(metadata);
        return jsonObject.getBoolean("defaultEventType");
      } catch (Exception e) {
        return false;
      }
    }).collect(Collectors.toList()).get(0);
  }
}