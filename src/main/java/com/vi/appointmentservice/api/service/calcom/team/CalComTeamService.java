package com.vi.appointmentservice.api.service.calcom.team;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vi.appointmentservice.api.exception.httpresponses.CalComApiException;
import com.vi.appointmentservice.api.model.CalcomMembership;
import com.vi.appointmentservice.api.model.CalcomTeam;
import com.vi.appointmentservice.api.service.calcom.CalComService;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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

  private String generateSlug(String name) {
    name = name.toLowerCase();
    String regex = "[^\\w]+"; // Relace everyting but word characters (digits, numbers)
    String subst = "-";
    Pattern pattern = Pattern.compile(regex);
    Matcher matcher = pattern.matcher(name);
    return matcher.replaceAll(subst);
  }

  public List<CalcomTeam> getAllTeams() {
    String response = this.restTemplate.getForObject(this.buildUri("/v1/teams"), String.class);
    JSONObject jsonObject;
    if (response != null) {
      jsonObject = new JSONObject(response);
    } else {
      throw new CalComApiException("Calcom team API response was null");
    }
    response = jsonObject.getJSONArray("teams").toString();
    ObjectMapper mapper = new ObjectMapper();
    try {
      CalcomTeam[] result = mapper.readValue(response, CalcomTeam[].class);
      return List.of(Objects.requireNonNull(result));
    } catch (JsonProcessingException e) {
      throw new CalComApiException("Could not deserialize teams response from calcom api");
    }

  }

  public CalcomTeam getTeamById(Long teamId) {
    String response = restTemplate.getForObject(this.buildUri("/v1/teams/" + teamId), String.class);
    JSONObject jsonObject;
    if (response != null) {
      jsonObject = new JSONObject(response);
    } else {
      throw new CalComApiException("Calcom team API response was null");
    }
    response = jsonObject.getJSONObject("team").toString();
    ObjectMapper mapper = new ObjectMapper();
    try {
      return mapper.readValue(response, CalcomTeam.class);
    } catch (JsonProcessingException e) {
      throw new CalComApiException("Could not deserialize team response from calcom api");
    }
  }

  public CalcomTeam createTeam(CalcomTeam team) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    JSONObject teamObject = new JSONObject();
    teamObject.put("name", team.getName());
    teamObject.put("slug", generateSlug(team.getName()));
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
      teamObject.put("slug", generateSlug(team.getName()));
    }
    HttpEntity<String> request = new HttpEntity<>(teamObject.toString(), headers);
    return restTemplate.patchForObject(this.buildUri("/v1/teams/" + team.getId()), request,
        TeamUpdateResponse.class).getTeam();
  }

  public void deleteTeam(Long teamId) {
    //TODO: the api is not working
    restTemplate.delete(this.buildUri("/v1/teams/" + teamId));
  }

  public CalcomMembership addUserToTeam(Long calComUserId, Long calComTeamid) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    JSONObject membership = new JSONObject();
    membership.put("teamId", calComTeamid);
    membership.put("userId", calComUserId);
    membership.put("accepted", true);
    membership.put("userId", "MEMBER");
    HttpEntity<String> request = new HttpEntity<>(membership.toString(), headers);
    return restTemplate.postForEntity(this.buildUri("/v1/memberships"), request,
        CalcomMembership.class).getBody();
  }


}