package com.vi.appointmentservice.api.service.onlineberatung;

import com.vi.appointmentservice.adapters.keycloak.dto.KeycloakLoginResponseDTO;
import com.vi.appointmentservice.api.service.securityheader.SecurityHeaderSupplier;
import com.vi.appointmentservice.port.out.IdentityClient;
import com.vi.appointmentservice.userservice.generated.ApiClient;
import com.vi.appointmentservice.userservice.generated.web.UserControllerApi;
import com.vi.appointmentservice.userservice.generated.web.model.ConsultantSearchResultDTO;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UserService {

  public static final Integer DIGI_CONSULTING_TYPE = 1;


  @Autowired
  @Qualifier("regularUser")
  public void setUserControllerApi(
      UserControllerApi userControllerApi) {
    this.userControllerApi = userControllerApi;
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

  private UserControllerApi userControllerApi;
  private SecurityHeaderSupplier securityHeaderSupplier;
  private IdentityClient identityClient;

  @Value("${keycloakService.technical.username}")
  private String keycloakTechnicalUsername;

  @Value("${keycloakService.technical.password}")
  private String keycloakTechnicalPassword;

  //TODO: why not return List<ConsultantDTO> ?
  //TODO: usecase and naming of this method are misleading. you use it for the initial
  // migration, but what if someone uses it for something else, but you fetch only 999 consultants
  // i would give it a more specific name
  public JSONArray getAllConsultants() {
    addTechnicalUserHeaders(userControllerApi.getApiClient());
    log.debug("Api Client: {}", userControllerApi.getApiClient().toString());
    ConsultantSearchResultDTO consultantsResponse = userControllerApi.searchConsultants(
        "*",
        1,
        999,
        "FIRSTNAME",
        "ASC"
    );
    JSONObject consultantSearchResultDTOJson = new JSONObject(consultantsResponse);
    log.debug("consultantSearchResultDTOJson: {}", consultantSearchResultDTOJson);
    JSONArray consultantsArray = consultantSearchResultDTOJson.getJSONArray("embedded");
    JSONArray consultantsResult = new JSONArray();
    for (int i = 0; i < consultantsArray.length(); i++) {
      consultantsResult.put(consultantsArray.getJSONObject(i).getJSONObject("embedded"));
    }
    return consultantsResult;
  }

  public String getRocketChatGroupId(String consultantId, String askerId) {
    addTechnicalUserHeaders(userControllerApi.getApiClient());
    return userControllerApi.getRocketChatGroupId(consultantId, askerId, DIGI_CONSULTING_TYPE).getGroupId();
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
