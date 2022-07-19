package com.vi.appointmentservice.api.service.calcom;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vi.appointmentservice.api.exception.httpresponses.BadRequestException;
import com.vi.appointmentservice.api.exception.httpresponses.CalComApiException;
import com.vi.appointmentservice.api.model.CalcomEventType;
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

  public List<CalcomEventType> getAllEventTypes() {
    String response = this.restTemplate.getForObject(this.buildUri("/v1/event-types"),
        String.class);
    JSONObject jsonObject;
    if (response != null) {
      jsonObject = new JSONObject(response);
    } else {
      throw new CalComApiException("Calcom event-type API response was null");
    }
    response = jsonObject.getJSONArray("event_types").toString();
    ObjectMapper mapper = new ObjectMapper();
    try {
      return mapper.readValue(response, new TypeReference<>() {
      });
    } catch (JsonProcessingException e) {
      throw new CalComApiException("Could not deserialize event-types response from calcom api");
    }

  }

  public void deleteAllEventTypesOfUser(Long userId) {
    List<CalcomEventType> eventTypesOfUser = new ArrayList<>(this.getAllEventTypes()).stream()
        .filter(eventType -> eventType.getUserId() != null
            && eventType.getUserId() == userId.intValue())
        .collect(Collectors.toList());
    for (CalcomEventType eventType : eventTypesOfUser) {
      this.deleteEventType(Long.valueOf(eventType.getId()));
    }
  }

  public List<CalcomEventType> getAllEventTypesOfTeam(Long teamId) {
    List<CalcomEventType> result = this.getAllEventTypes();
    // TODO: Add correct filter: .filter(eventType -> eventType.getTeamId() != null && eventType.getTeamId() == teamId.intValue())
    return new ArrayList<>(result).stream()
        .filter(eventType -> eventType.getTeamId() != null)
        .collect(Collectors.toList());
  }

  public List<CalcomEventType> getAllEventTypesOfUser(Long userId) {
    List<CalcomEventType> result = this.getAllEventTypes();
    return new ArrayList<>(result).stream()
        .filter(eventType -> eventType.getUserId() != null
            && eventType.getUserId() == userId.intValue())
        .collect(Collectors.toList());
  }

  public CalcomEventType getEventTypeById(Long eventTypeId) {
    List<CalcomEventType> result = this.getAllEventTypes();
    CalcomEventType found = new ArrayList<>(result).stream()
        .filter(eventType -> eventType.getId() == eventTypeId.intValue())
        .collect(Collectors.toList()).get(0);
    if (found != null) {
      return found;
    } else {
      throw new BadRequestException(
          String.format("No calcom event-type found for id '%s'", eventTypeId));
    }

  }

  public CalcomEventType createEventType(JSONObject eventType) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<String> request = new HttpEntity<>(eventType.toString(), headers);
    return restTemplate.postForEntity(this.buildUri("/v1/event-types"), request,
        CalcomEventType.class).getBody();
  }

  public CalcomEventType editEventType(JSONObject eventType) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<String> request = new HttpEntity<>(eventType.toString(), headers);
    return restTemplate.postForEntity(this.buildUri("/v1/event-types/" + eventType.get("id")),
        request, CalcomEventType.class).getBody();
  }

  public HttpStatus deleteEventType(Long eventTypeId) {
    return restTemplate.exchange(this.buildUri("/v1/event-types/" + eventTypeId), HttpMethod.DELETE,
        null, String.class).getStatusCode();
  }


}