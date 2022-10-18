package com.vi.appointmentservice.api.service.onlineberatung;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vi.appointmentservice.adapters.keycloak.dto.KeycloakLoginResponseDTO;
import com.vi.appointmentservice.api.exception.httpresponses.InternalServerErrorException;
import com.vi.appointmentservice.api.service.securityheader.SecurityHeaderSupplier;
import com.vi.appointmentservice.config.AdminUserApiClient;
import com.vi.appointmentservice.port.out.IdentityClient;
import com.vi.appointmentservice.useradminservice.generated.ApiClient;
import com.vi.appointmentservice.useradminservice.generated.web.AdminUserControllerApi;
import com.vi.appointmentservice.useradminservice.generated.web.model.AskerResponseDTO;
import com.vi.appointmentservice.useradminservice.generated.web.model.ConsultantDTO;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

@Service
@Slf4j
public class AdminUserService {

  @Value("${user.admin.service.api.url}")
  private String adminUserServiceApiUrl;

  @Autowired
  public void setSecurityHeaderSupplier(
      SecurityHeaderSupplier securityHeaderSupplier) {
    this.securityHeaderSupplier = securityHeaderSupplier;
  }

  @Autowired
  public void setIdentityClient(IdentityClient identityClient) {
    this.identityClient = identityClient;
  }

  private SecurityHeaderSupplier securityHeaderSupplier;
  private IdentityClient identityClient;

  @Value("${keycloakService.technical.username}")
  private String keycloakTechnicalUsername;

  @Value("${keycloakService.technical.password}")
  private String keycloakTechnicalPassword;

  @Autowired
  RestTemplate restTemplate;

  public Map<String, String> getConsultantNamesForIds(Collection<String> consultantIds) {
    Map<String, String> consultantNames = new HashMap<>();
    var adminUserControllerApi = getAdminUserControllerApi();
    addTechnicalUserHeaders(adminUserControllerApi.getApiClient());
    consultantIds.stream().forEach(consultantId -> {
      String consultantResponse = new JSONObject(
          adminUserControllerApi.getConsultant(consultantId)).getJSONObject("embedded").toString();
      try {
        ObjectMapper mapper = new ObjectMapper().configure(
            DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        ConsultantDTO consultantDTO = mapper.readValue(consultantResponse, ConsultantDTO.class);
        consultantNames
            .put(consultantId, consultantDTO.getFirstname() + " " + consultantDTO.getLastname());
      } catch (JsonProcessingException e) {
        consultantNames
            .put(consultantId, "Unknown Consultant");
      }
    });

    return consultantNames;
  }

  public Map<String, String> getAskerUserNamesForIds(Collection<String> askerIds) {
    Map<String, String> result = new HashMap<>();
    var adminUserControllerApi = getAdminUserControllerApi();
    addTechnicalUserHeaders(adminUserControllerApi.getApiClient());
    askerIds.stream().forEach(askerId -> {
      try {
        AskerResponseDTO asker = adminUserControllerApi.getAsker(askerId);
        result.put(askerId, asker.getUsername());
      } catch (ResponseStatusException ex) {
        if (HttpStatus.NOT_FOUND.equals(ex.getStatus())) {
          result.put(askerId, "Unknown asker");
          log.warn("Asker with username {} not found in userservice DB");
        }
      }
    });
    return result;
  }

  private void addTechnicalUserHeaders(ApiClient apiClient) {
    KeycloakLoginResponseDTO keycloakLoginResponseDTO = identityClient.loginUser(
        keycloakTechnicalUsername, keycloakTechnicalPassword
    );
    log.debug("Technical Acces Token: {}", keycloakLoginResponseDTO.getAccessToken());
    HttpHeaders headers = this.securityHeaderSupplier
        .getKeycloakAndCsrfHttpHeaders(keycloakLoginResponseDTO.getAccessToken());
    headers.forEach((key, value) -> apiClient.addDefaultHeader(key, value.iterator().next()));
  }

  public AdminUserControllerApi getAdminUserControllerApi() {
    ApiClient apiClient = new AdminUserApiClient(restTemplate);
    apiClient.setBasePath(this.adminUserServiceApiUrl);
    return new AdminUserControllerApi(apiClient);
  }

  public ConsultantDTO getConsultantById(String consultantId) {
    var adminUserControllerApi = getAdminUserControllerApi();
    addTechnicalUserHeaders(adminUserControllerApi.getApiClient());
    String consultantResponse = new JSONObject(
        adminUserControllerApi.getConsultant(consultantId)).getJSONObject("embedded").toString();
    ObjectMapper mapper = new ObjectMapper().configure(
        DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    try {
      return mapper.readValue(consultantResponse, ConsultantDTO.class);
    } catch (JsonProcessingException e) {
      throw new InternalServerErrorException("Could not deserialize consultant response from userService");
    }
  }

}
