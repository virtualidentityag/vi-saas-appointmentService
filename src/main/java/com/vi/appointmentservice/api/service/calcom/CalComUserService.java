package com.vi.appointmentservice.api.service.calcom;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vi.appointmentservice.api.exception.httpresponses.BadRequestException;
import com.vi.appointmentservice.api.exception.httpresponses.CalComApiErrorException;
import com.vi.appointmentservice.api.model.CalcomUser;
import lombok.extern.slf4j.Slf4j;
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
import org.springframework.web.client.HttpClientErrorException;
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
    } catch (HttpClientErrorException e){
      log.warn("Calcom user creation field conflic (possibly email)");
      return null;
    } catch (Exception e) {
      throw new CalComApiErrorException("Could not create calcom user");
    }
  }

  public HttpStatus deleteUser(Long userId) {
    return restTemplate
        .exchange(this.buildUri("/v1/users/" + userId), HttpMethod.DELETE, null, String.class)
        .getStatusCode();
  }


  public CalcomUser getUserById(Long userId) {
    ObjectMapper mapper = new ObjectMapper();
    ResponseEntity<java.lang.String> response = restTemplate
        .exchange(this.buildUri("/v1/users/" + userId), HttpMethod.GET, null, String.class);
    if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
      JSONObject jsonObject = new JSONObject(response.getBody());
      try {
        return mapper.readValue(jsonObject.getJSONObject("user").toString(), CalcomUser.class);
      } catch (Exception e) {
        throw new CalComApiErrorException("Could not deserialize user response from calcom api");
      }
    } else {
      throw new BadRequestException(
          String.format("No calcom user associated to consultant id '%s'", userId));
    }
  }
}
