package com.vi.appointmentservice.api.service.calcom;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vi.appointmentservice.api.exception.httpresponses.CalComApiErrorException;
import com.vi.appointmentservice.api.model.CalcomMembership;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class CalComMembershipService extends CalComService {

  public CalComMembershipService(@NonNull RestTemplate restTemplate,
      @Value("${calcom.apiUrl}") String calcomApiUrl,
      @Value("${calcom.apiKey}") String calcomApiKey, @NonNull ObjectMapper objectMapper) {
    super(restTemplate, calcomApiUrl, calcomApiKey);
  }

  public List<CalcomMembership> getAllMemberships() {
    String response = this.restTemplate.getForObject(this.buildUri("/v1/memberships"),
        String.class);
    JSONObject jsonObject = new JSONObject(response);
    response = jsonObject.getJSONArray("memberships").toString();
    ObjectMapper mapper = new ObjectMapper();
    List<CalcomMembership> result = null;
    try {
      result = mapper.readValue(response, new TypeReference<>() {
      });
    } catch (JsonProcessingException e) {
      throw new CalComApiErrorException("Could not deserialize membership response from calcom api");
    }
    return result;
  }

  public void deleteAllMembershipsOfUser(Long userId) {
    List<CalcomMembership> membershipsOfUser = new ArrayList<>(this.getAllMemberships()).stream()
        .filter(membership -> membership.getUserId() != null
            && membership.getUserId() == userId.intValue()).collect(Collectors.toList());
    for (CalcomMembership membership : membershipsOfUser) {
      this.deleteMembership(membership.getUserId(), membership.getTeamId());
    }
  }

  public void deleteMembership(Long userId, Long teamId) {
    restTemplate.exchange(this.buildUri("/v1/memberships/" + userId + "_" + teamId),
        HttpMethod.DELETE, null, String.class).getStatusCode();
  }

}