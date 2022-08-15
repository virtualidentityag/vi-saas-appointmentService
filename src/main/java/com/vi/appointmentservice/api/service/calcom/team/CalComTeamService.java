package com.vi.appointmentservice.api.service.calcom.team;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vi.appointmentservice.api.exception.httpresponses.CalComApiErrorException;
import com.vi.appointmentservice.api.model.CalcomTeam;
import com.vi.appointmentservice.api.service.calcom.CalComService;
import java.util.UUID;
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

@Service
@Slf4j
public class CalComTeamService extends CalComService {

  @Autowired
  public CalComTeamService(RestTemplate restTemplate,
      @Value("${calcom.apiUrl}") String calcomApiUrl,
      @Value("${calcom.apiKey}") String calcomApiKey) {
    super(restTemplate, calcomApiUrl, calcomApiKey);
  }

  public CalcomTeam getTeamById(Long teamId) {
    String response = restTemplate.getForObject(this.buildUri("/v1/teams/" + teamId), String.class);
    JSONObject jsonObject;
    if (response != null) {
      jsonObject = new JSONObject(response);
    } else {
      throw new CalComApiErrorException("Calcom team API response was null");
    }
    response = jsonObject.getJSONObject("team").toString();
    ObjectMapper mapper = new ObjectMapper();
    try {
      return mapper.readValue(response, CalcomTeam.class);
    } catch (JsonProcessingException e) {
      throw new CalComApiErrorException("Could not deserialize team response from calcom api");
    }
  }

  public CalcomTeam createTeam(CalcomTeam team) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    JSONObject teamObject = new JSONObject();
    teamObject.put("name", team.getName());
    teamObject.put("slug", UUID.randomUUID().toString());
    teamObject.put("hideBranding", team.getHideBranding());
    HttpEntity<String> request = new HttpEntity<>(teamObject.toString(), headers);
    return restTemplate.postForEntity(this.buildUri("/v1/teams"), request, TeamUpdateResponse.class)
        .getBody().getTeam();
  }

  public CalcomTeam editTeam(CalcomTeam team) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    JSONObject teamObject = new JSONObject();
    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    if (team.getName() != null) {
      teamObject.put("name", team.getName());
    }
    HttpEntity<String> request = new HttpEntity<>(teamObject.toString(), headers);
    return restTemplate.patchForObject(this.buildUri("/v1/teams/" + team.getId()), request,
        TeamUpdateResponse.class).getTeam();
  }

}