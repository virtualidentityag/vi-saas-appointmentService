package com.vi.appointmentservice.api.service.onlineberatung;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vi.appointmentservice.adapters.keycloak.dto.KeycloakLoginResponseDTO;
import com.vi.appointmentservice.api.exception.httpresponses.InternalServerErrorException;
import com.vi.appointmentservice.port.out.IdentityClient;
import com.vi.appointmentservice.api.service.securityheader.SecurityHeaderSupplier;
import com.vi.appointmentservice.useradminservice.generated.ApiClient;
import com.vi.appointmentservice.useradminservice.generated.web.AdminUserControllerApi;
import com.vi.appointmentservice.useradminservice.generated.web.model.AskerResponseDTO;
import com.vi.appointmentservice.useradminservice.generated.web.model.ConsultantDTO;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AdminUserService {

  @Qualifier("adminUser")
  @Autowired
  public void setAdminUserControllerApi(
      AdminUserControllerApi adminUserControllerApi) {
    this.adminUserControllerApi = adminUserControllerApi;
  }

  @Autowired
  public void setSecurityHeaderSupplier(
      SecurityHeaderSupplier securityHeaderSupplier) {
    this.securityHeaderSupplier = securityHeaderSupplier;
  }

  @Autowired
  public void setIdentityClient(IdentityClient identityClient) {
    this.identityClient = identityClient;
  }

  private AdminUserControllerApi adminUserControllerApi;
  private SecurityHeaderSupplier securityHeaderSupplier;
  private IdentityClient identityClient;

  @Value("${keycloakService.technical.username}")
  private String keycloakTechnicalUsername;

  @Value("${keycloakService.technical.password}")
  private String keycloakTechnicalPassword;

  public ConsultantDTO getConsultantById(String consultantId) {
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

  public AskerResponseDTO getAskerById(String askerId) {
    addTechnicalUserHeaders(adminUserControllerApi.getApiClient());
    return adminUserControllerApi.getAsker(askerId);
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
}
