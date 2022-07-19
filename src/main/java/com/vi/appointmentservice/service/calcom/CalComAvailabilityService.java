package com.vi.appointmentservice.service.calcom;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vi.appointmentservice.api.model.CalcomAvailability;
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
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class CalComAvailabilityService extends CalComService {

  public CalComAvailabilityService(@NonNull RestTemplate restTemplate,
      @Value("${calcom.apiUrl}") String calcomApiUrl,
      @Value("${calcom.apiKey}") String calcomApiKey, @NonNull ObjectMapper objectMapper) {
    super(restTemplate, calcomApiUrl, calcomApiKey);
  }

  public List<CalcomAvailability> getAllAvailabilities() throws JsonProcessingException {
    String response = this.restTemplate.getForObject(this.buildUri("/v1/availabilities"),
        String.class);
    JSONObject jsonObject = new JSONObject(response);
    response = jsonObject.getJSONArray("availabilities").toString();
    ObjectMapper mapper = new ObjectMapper();
    List<CalcomAvailability> result = mapper.readValue(response,
        new TypeReference<List<CalcomAvailability>>() {
        });
    return result;
  }

  public CalcomAvailability getAvailabilityById(Long availabilityId)
      throws JsonProcessingException {
    List<CalcomAvailability> result = this.getAllAvailabilities();
    return new ArrayList<>(result).stream()
        .filter(availability -> availability.getId() == availabilityId.intValue())
        .collect(Collectors.toList()).get(0);
  }

  public CalcomAvailability createAvailability(JSONObject availability) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<String> request = new HttpEntity<>(availability.toString(), headers);
    return restTemplate.postForEntity(this.buildUri("/v1/availabilities"), request,
        CalcomAvailability.class).getBody();
  }

  public CalcomAvailability editAvailability(JSONObject availability) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    HttpEntity<String> request = new HttpEntity<>(availability.toString(), headers);
    return restTemplate.postForEntity(this.buildUri(
            "/v1/availabilities/" + availability.get("userId") + "_" + availability.get("teamId")),
        request, CalcomAvailability.class).getBody();
  }

  public void deleteAvailability(Integer availabilityId) {
    restTemplate.exchange(this.buildUri("/v1/availabilities/" + availabilityId),
        HttpMethod.DELETE, null, String.class).getStatusCode();
  }

}