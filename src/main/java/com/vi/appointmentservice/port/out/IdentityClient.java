package com.vi.appointmentservice.port.out;

import com.vi.appointmentservice.adapters.keycloak.dto.KeycloakLoginResponseDTO;

public interface IdentityClient {
  KeycloakLoginResponseDTO loginUser(final String userName, final String password);
}
