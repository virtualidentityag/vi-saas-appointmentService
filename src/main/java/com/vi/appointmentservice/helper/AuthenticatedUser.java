package com.vi.appointmentservice.helper;

import lombok.*;

/**
 * Representation of the via Keyclcoak authentificated user
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AuthenticatedUser {

  @NonNull
  private String userId;

  @NonNull
  private String username;

  @NonNull
  private String accessToken;

}
