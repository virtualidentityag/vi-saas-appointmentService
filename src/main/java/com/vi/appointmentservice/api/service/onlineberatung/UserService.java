package com.vi.appointmentservice.api.service.onlineberatung;

import com.vi.appointmentservice.adapters.keycloak.dto.KeycloakLoginResponseDTO;
import com.vi.appointmentservice.api.service.securityheader.SecurityHeaderSupplier;
import com.vi.appointmentservice.config.UserApiClient;
import com.vi.appointmentservice.port.out.IdentityClient;
import com.vi.appointmentservice.userservice.generated.ApiClient;
import com.vi.appointmentservice.userservice.generated.web.UserControllerApi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class UserService {

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

  @Value("${user.service.api.url}")
  private String userServiceApiUrl;

  @Autowired RestTemplate restTemplate;

  public String getRocketChatGroupId(String consultantId, String askerId) {
    var userControllerApi = getUserControllerApi();
    addTechnicalUserHeaders(userControllerApi.getApiClient());
    return userControllerApi.getRocketChatGroupId(consultantId, askerId).getGroupId();
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

  public UserControllerApi getUserControllerApi() {
    ApiClient apiClient = new UserApiClient(restTemplate);
    apiClient.setBasePath(this.userServiceApiUrl);
    return new UserControllerApi(apiClient);
  }

  private void addDefaultHeaders(ApiClient apiClient, String authenticatedUserToken) {
    HttpHeaders headers = this.securityHeaderSupplier.getKeycloakAndCsrfHttpHeaders(authenticatedUserToken);
    headers.forEach((key, value) -> apiClient.addDefaultHeader(key, value.iterator().next()));
  }

  public com.vi.appointmentservice.userservice.generated.web.AppointmentControllerApi getUserAppointmentApi(
      String authenticatedUserToken) {
    ApiClient apiClient = new UserApiClient(restTemplate);
    apiClient.setBasePath(this.userServiceApiUrl);
    addDefaultHeaders(apiClient, authenticatedUserToken);
    return new com.vi.appointmentservice.userservice.generated.web.AppointmentControllerApi(apiClient);
  }
}
