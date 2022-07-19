package com.vi.appointmentservice.api.service.calcom;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vi.appointmentservice.api.model.CalcomUser;
import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
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
public class CalComUserService extends CalComService {

  @Autowired
  public CalComUserService(RestTemplate restTemplate,
      @Value("${calcom.apiUrl}") String calcomApiUrl,
      @Value("${calcom.apiKey}") String calcomApiKey) {
    super(restTemplate, calcomApiUrl, calcomApiKey);
  }

  // Users
  public List<CalcomUser> getUsers() throws JsonProcessingException {
    //TODO: not used as i see
    // ResponseEntity<CalcomUser[]> response = restTemplate.getForEntity(String.format(this.buildUri("/users"), calcomApiUrl, calcomApiKey), CalcomUser[].class);
    String response = this.restTemplate.getForObject(this.buildUri("/v1/users"), String.class);
    JSONObject jsonObject = new JSONObject(response);
    response = jsonObject.getJSONArray("users").toString();
    ObjectMapper mapper = new ObjectMapper();
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    CalcomUser[] result = mapper.readValue(response, CalcomUser[].class);
    return List.of(Objects.requireNonNull(result));
  }


  public CalcomUser createUser(JSONObject user) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    log.info("Creating calcom user: {}", user);
    HttpEntity<String> request = new HttpEntity<>(user.toString(), headers);
    try {
      ResponseEntity<String> response = restTemplate
          .exchange(this.buildUri("/v1/users"), HttpMethod.POST, request, String.class);
      JSONObject jsonObject = new JSONObject(response.getBody());
      String body = jsonObject.getJSONObject("user").toString();
      ObjectMapper mapper = new ObjectMapper();
      mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
      return mapper.readValue(body, CalcomUser.class);
    } catch (Exception e) {
      log.error(ExceptionUtils.getStackTrace(e));
      //TODO: in case an exception happens during a call to calcom the frontend will get
      // status 200.
      return null;
    }
  }

  public CalcomUser updateUser(JSONObject user) throws JsonProcessingException {
    long userId = user.getLong("id");
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    log.info("updating calcom user: {}", user);
    user.remove("id");
    user.remove("email");
    HttpEntity<String> request = new HttpEntity<>(user.toString(), headers);
    try {
      ResponseEntity<String> response = restTemplate
          .exchange(this.buildUri("/v1/users/" + userId), HttpMethod.PATCH, request,
              String.class);
      String body = response.getBody();
      JSONObject jsonObject = new JSONObject(body);
      body = jsonObject.getJSONObject("user").toString();
      ObjectMapper mapper = new ObjectMapper();
      mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
      return mapper.readValue(body, CalcomUser.class);
    } catch (Exception e) {
      log.error(ExceptionUtils.getStackTrace(e));
        //TODO: same here as on creation. did not check it but i suppose its the same.
        // in case an exception happens we have to stop the request and throw some RuntimeException
        // that is going to be transformed either some 50* HTTP ERROR
      return null;
    }
  }

  public HttpStatus deleteUser(Long userId) {
    return restTemplate
        .exchange(this.buildUri("/v1/users/" + userId), HttpMethod.DELETE, null, String.class)
        .getStatusCode();
  }


  public CalcomUser getUserById(Long userId) throws JsonProcessingException {
    ObjectMapper mapper = new ObjectMapper();
    ResponseEntity<java.lang.String> response = restTemplate
        .exchange(this.buildUri("/v1/users/" + userId), HttpMethod.GET, null, String.class);
    if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
      JSONObject jsonObject = new JSONObject(response.getBody());
      if (jsonObject.getJSONObject("user") != null) {
        return mapper.readValue(jsonObject.getJSONObject("user").toString(), CalcomUser.class);
      }
    }
    return null;
  }
}
