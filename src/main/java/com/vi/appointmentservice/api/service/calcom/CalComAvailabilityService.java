package com.vi.appointmentservice.api.service.calcom;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
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

  public void deleteAvailability(Integer availabilityId) {
    restTemplate.exchange(this.buildUri("/v1/availabilities/" + availabilityId),
        HttpMethod.DELETE, null, String.class).getStatusCode();
  }

}