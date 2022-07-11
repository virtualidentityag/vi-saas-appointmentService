package com.vi.appointmentservice.adapters.keycloak;

import com.vi.appointmentservice.adapters.keycloak.dto.KeycloakLoginResponseDTO;
import com.vi.appointmentservice.helper.AuthenticatedUser;
import com.vi.appointmentservice.port.out.IdentityClient;
import com.vi.appointmentservice.port.out.IdentityClientConfig;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import javax.ws.rs.BadRequestException;

import static com.vi.appointmentservice.helper.RequestHelper.getAuthorizedHttpHeaders;
import static com.vi.appointmentservice.helper.RequestHelper.getFormHttpHeaders;


/**
 * Service for Keycloak REST API calls.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class KeycloakService implements IdentityClient {

    private static final String KEYCLOAK_GRANT_TYPE_PW = "password";
    private static final String KEYCLOAK_GRANT_TYPE_REFRESH_TOKEN = "refresh_token";
    private static final String BODY_KEY_USERNAME = "username";
    private static final String BODY_KEY_PASSWORD = "password";
    private static final String BODY_KEY_CLIENT_ID = "client_id";
    private static final String BODY_KEY_GRANT_TYPE = "grant_type";
    private static final String ENDPOINT_OPENID_CONNECT_LOGIN = "/token";
    private static final String ENDPOINT_OPENID_CONNECT_LOGOUT = "/logout";
    ;

    private final @NonNull RestTemplate restTemplate;
    private final @NonNull AuthenticatedUser authenticatedUser;
    private final @NonNull IdentityClientConfig identityClientConfig;


    @Value("${keycloak.config.app-client-id}")
    private String keycloakClientId;

    /**
     * Performs a Keycloak login and returns the Keycloak {@link KeycloakLoginResponseDTO} on
     * success.
     *
     * @param userName the username
     * @param password the password
     * @return {@link KeycloakLoginResponseDTO}
     */
    public KeycloakLoginResponseDTO loginUser(final String userName, final String password) {

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add(BODY_KEY_USERNAME, userName);
        map.add(BODY_KEY_PASSWORD, password);
        map.add(BODY_KEY_CLIENT_ID, keycloakClientId);
        map.add(BODY_KEY_GRANT_TYPE, KEYCLOAK_GRANT_TYPE_PW);
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, getFormHttpHeaders());

        try {
            return restTemplate
                    .postForEntity(
                            identityClientConfig.getOpenIdConnectUrl(ENDPOINT_OPENID_CONNECT_LOGIN),
                            request, KeycloakLoginResponseDTO.class
                    )
                    .getBody();

        } catch (RestClientResponseException exception) {
            throw new BadRequestException(String.format("Could not log in user %s into Keycloak: %s",
                    userName, exception.getMessage()), exception);
        }
    }

    /**
     * Performs a Keycloak logout. This only destroys the Keycloak session, the (offline) access token
     * will still be valid until expiration date/time ends.
     *
     * @param refreshToken the refreshToken
     * @return true if logout was successful
     */
    public boolean logoutUser(final String refreshToken) {

        var httpHeaders =
                getAuthorizedHttpHeaders(authenticatedUser.getAccessToken(),
                        MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add(BODY_KEY_CLIENT_ID, keycloakClientId);
        map.add(BODY_KEY_GRANT_TYPE, KEYCLOAK_GRANT_TYPE_REFRESH_TOKEN);
        map.add(KEYCLOAK_GRANT_TYPE_REFRESH_TOKEN, refreshToken);
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, httpHeaders);

        var url = identityClientConfig.getOpenIdConnectUrl(ENDPOINT_OPENID_CONNECT_LOGOUT);
        try {
            var response = restTemplate.postForEntity(url, request, Void.class);
            return wasLogoutSuccessful(response, refreshToken);
        } catch (Exception ex) {
            log.error("Keycloak error: Could not log out user with refresh token {}", refreshToken, ex);

            return false;
        }
    }

    private boolean wasLogoutSuccessful(ResponseEntity<Void> responseEntity, String refreshToken) {
        if (!responseEntity.getStatusCode().equals(HttpStatus.NO_CONTENT)) {
            log.error("Keycloak error: Could not log out user with refresh token {}", refreshToken);
            return false;
        }
        return true;
    }
}
