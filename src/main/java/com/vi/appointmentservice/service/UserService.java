package com.vi.appointmentservice.service;

import com.vi.appointmentservice.adapters.keycloak.dto.KeycloakLoginResponseDTO;
import com.vi.appointmentservice.port.out.IdentityClient;
import com.vi.appointmentservice.service.securityheader.SecurityHeaderSupplier;
import com.vi.appointmentservice.userservice.generated.web.UserControllerApi;
import com.vi.appointmentservice.userservice.generated.ApiClient;
import com.vi.appointmentservice.userservice.generated.web.model.ConsultantSearchResultDTO;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {
    private final @NonNull UserControllerApi userControllerApi;
    private final @NonNull SecurityHeaderSupplier securityHeaderSupplier;
    private final @NonNull IdentityClient identityClient;

    @Value("${keycloakService.technical.username}")
    private String keycloakTechnicalUsername;

    @Value("${keycloakService.technical.password}")
    private String keycloakTechnicalPassword;

    public ConsultantSearchResultDTO getAllConsultants() {
        addTechnicalUserHeaders(userControllerApi.getApiClient());
        ConsultantSearchResultDTO consultants = userControllerApi.searchConsultants(
                "",
                1,
                999,
                "",
                ""
        );
        log.debug(String.valueOf(consultants));
        return consultants;
    }

    private void addTechnicalUserHeaders(ApiClient apiClient) {
        KeycloakLoginResponseDTO keycloakLoginResponseDTO = identityClient.loginUser(
                keycloakTechnicalUsername, keycloakTechnicalPassword
        );
        var headers = this.securityHeaderSupplier
                .getKeycloakAndCsrfHttpHeaders(keycloakLoginResponseDTO.getAccessToken());
        headers.forEach((key, value) -> apiClient.addDefaultHeader(key, value.iterator().next()));
    }
}
