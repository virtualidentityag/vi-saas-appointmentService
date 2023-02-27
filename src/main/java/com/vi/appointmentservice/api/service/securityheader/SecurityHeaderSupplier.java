package com.vi.appointmentservice.api.service.securityheader;

import com.vi.appointmentservice.helper.AuthenticatedUser;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SecurityHeaderSupplier {

  private final @NonNull AuthenticatedUser authenticatedUser;

  @Value("${csrf.header.property}")
  private String csrfHeaderProperty;

  @Value("${csrf.cookie.property}")
  private String csrfCookieProperty;

  /**
   * Creates the headers containing keycloak token of technical user and csrf headers {@link
   * HttpHeaders} object.
   *
   * @param accessToken the token used for keycloak authorization header
   * @return the created {@link HttpHeaders}
   */
  public HttpHeaders getKeycloakAndCsrfHttpHeaders(String accessToken) {
    var header = getCsrfHttpHeaders();
    this.addKeycloakAuthorizationHeader(header, accessToken);

    return header;
  }

  public HttpHeaders getKeycloakAndCsrfHttpHeaders() {
    var header = getCsrfHttpHeaders();
    this.addKeycloakAuthorizationHeader(header, authenticatedUser.getAccessToken());
    return header;
  }

  public HttpHeaders getCsrfHttpHeaders() {
    var httpHeaders = new HttpHeaders();

    return this.addCsrfValues(httpHeaders);
  }

  private HttpHeaders addCsrfValues(HttpHeaders httpHeaders) {
    var csrfToken = UUID.randomUUID().toString();

    httpHeaders.setContentType(MediaType.APPLICATION_JSON);
    httpHeaders.add("Cookie", csrfCookieProperty + "=" + csrfToken);
    httpHeaders.add(csrfHeaderProperty, csrfToken);

    return httpHeaders;
  }

  private void addKeycloakAuthorizationHeader(HttpHeaders httpHeaders, String accessToken) {
    httpHeaders.add("Authorization", "Bearer " + accessToken);
  }
}
