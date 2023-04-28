package com.vi.appointmentservice.api.authorization;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Definition of all authorities and of the role-authority-mapping.
 */
@AllArgsConstructor
@Getter
public enum Authority {

  ANONYMOUS(UserRole.ANONYMOUS, singletonList(AuthorityValue.ANONYMOUS_DEFAULT)),
  USER(UserRole.USER, List.of(
      AuthorityValue.USER_DEFAULT, AuthorityValue.ASSIGN_CONSULTANT_TO_SESSION)),
  CONSULTANT(UserRole.CONSULTANT,
      List.of(AuthorityValue.CONSULTANT_DEFAULT, AuthorityValue.ASSIGN_CONSULTANT_TO_SESSION, AuthorityValue.VIEW_AGENCY_CONSULTANTS)),
  PEER_CONSULTANT(UserRole.PEER_CONSULTANT, singletonList(AuthorityValue.USE_FEEDBACK)),
  MAIN_CONSULTANT(UserRole.MAIN_CONSULTANT,
      List.of(AuthorityValue.VIEW_ALL_FEEDBACK_SESSIONS, AuthorityValue.VIEW_ALL_PEER_SESSIONS, AuthorityValue.ASSIGN_CONSULTANT_TO_ENQUIRY,
          AuthorityValue.ASSIGN_CONSULTANT_TO_PEER_SESSION)),
  TECHNICAL(UserRole.TECHNICAL, singletonList(AuthorityValue.TECHNICAL_DEFAULT)),
  GROUP_CHAT_CONSULTANT(UserRole.GROUP_CHAT_CONSULTANT,
      List.of(
          AuthorityValue.CONSULTANT_DEFAULT, AuthorityValue.CREATE_NEW_CHAT, AuthorityValue.START_CHAT, AuthorityValue.STOP_CHAT, AuthorityValue.UPDATE_CHAT)),
  USER_ADMIN(UserRole.USER_ADMIN, singletonList(AuthorityValue.USER_ADMIN)),
  SINGLE_TENANT_ADMIN(UserRole.SINGLE_TENANT_ADMIN, singletonList(AuthorityValue.SINGLE_TENANT_ADMIN)),
  TENANT_ADMIN(UserRole.TENANT_ADMIN, singletonList(AuthorityValue.TENANT_ADMIN)),
  RESTRICTED_AGENCY_ADMIN(
      UserRole.RESTRICTED_AGENCY_ADMIN, singletonList(AuthorityValue.RESTRICTED_AGENCY_ADMIN));

  private final UserRole userRole;
  private final List<String> grantedAuthorities;

  public static List<String> getAuthoritiesByUserRole(UserRole userRole) {
    Optional<Authority> authorityByUserRole = Stream.of(values())
        .filter(authority -> authority.userRole.equals(userRole))
        .findFirst();

    return authorityByUserRole.isPresent() ? authorityByUserRole.get().getGrantedAuthorities()
        : emptyList();
  }


  /**
   * Get all authorities for a specific role.
   *
   * @return the authorities for current role
   **/
  public String getAuthority() {
    return this.userRole.toString();
  }

  public static class AuthorityValue {

    private AuthorityValue() {
    }

    public static final String PREFIX = "AUTHORIZATION_";
    public static final String ANONYMOUS_DEFAULT = PREFIX + "ANONYMOUS_DEFAULT";
    public static final String USER_DEFAULT = PREFIX + "USER_DEFAULT";
    public static final String CONSULTANT_DEFAULT = PREFIX + "CONSULTANT_DEFAULT";
    public static final String USE_FEEDBACK = PREFIX + "USE_FEEDBACK";
    public static final String VIEW_ALL_FEEDBACK_SESSIONS = PREFIX + "VIEW_ALL_FEEDBACK_SESSIONS";
    public static final String VIEW_ALL_PEER_SESSIONS = PREFIX + "VIEW_ALL_PEER_SESSIONS";
    public static final String ASSIGN_CONSULTANT_TO_SESSION =
        PREFIX + "ASSIGN_CONSULTANT_TO_SESSION";
    public static final String ASSIGN_CONSULTANT_TO_ENQUIRY =
        PREFIX + "ASSIGN_CONSULTANT_TO_ENQUIRY";
    public static final String ASSIGN_CONSULTANT_TO_PEER_SESSION =
        PREFIX + "ASSIGN_CONSULTANT_TO_PEER_SESSION";
    public static final String VIEW_AGENCY_CONSULTANTS = PREFIX + "VIEW_AGENCY_CONSULTANTS";
    public static final String TECHNICAL_DEFAULT = PREFIX + "TECHNICAL_DEFAULT";
    public static final String CREATE_NEW_CHAT = PREFIX + "CREATE_NEW_CHAT";
    public static final String START_CHAT = PREFIX + "START_CHAT";
    public static final String STOP_CHAT = PREFIX + "STOP_CHAT";
    public static final String UPDATE_CHAT = PREFIX + "UPDATE_CHAT";
    public static final String USER_ADMIN = PREFIX + "USER_ADMIN";
    public static final String SINGLE_TENANT_ADMIN = PREFIX + "SINGLE_TENANT_ADMIN";

    public static final String RESTRICTED_AGENCY_ADMIN = PREFIX + "RESTRICTED_AGENCY_ADMIN";

    public static final String TENANT_ADMIN = PREFIX + "TENANT_ADMIN";
  }

}
