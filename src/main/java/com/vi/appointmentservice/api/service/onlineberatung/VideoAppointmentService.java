package com.vi.appointmentservice.api.service.onlineberatung;

import com.vi.appointmentservice.adapters.keycloak.dto.KeycloakLoginResponseDTO;
import com.vi.appointmentservice.api.service.securityheader.SecurityHeaderSupplier;
import com.vi.appointmentservice.appointmentservice.generated.ApiClient;
import com.vi.appointmentservice.appointmentservice.generated.web.AppointmentControllerApi;
import com.vi.appointmentservice.appointmentservice.generated.web.model.Appointment;
import com.vi.appointmentservice.appointmentservice.generated.web.model.AppointmentStatus;
import com.vi.appointmentservice.port.out.IdentityClient;
import java.time.OffsetDateTime;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class VideoAppointmentService {

  private final @NonNull AppointmentControllerApi appointmentControllerApi;
  private final @NonNull IdentityClient identityClient;
  private final @NonNull SecurityHeaderSupplier securityHeaderSupplier;

  @Value("${keycloakService.technical.username}")
  private String keycloakTechnicalUsername;

  @Value("${keycloakService.technical.password}")
  private String keycloakTechnicalPassword;

  public Appointment createAppointment(String consultantEmail, OffsetDateTime startTime) {
    Appointment appointment = new Appointment();
    appointment.setConsultantEmail(consultantEmail);
    appointment.setStatus(AppointmentStatus.CREATED);
    appointment.setDatetime(startTime);
    addTechnicalUserHeaders(appointmentControllerApi.getApiClient());
    return appointmentControllerApi.createAppointment(appointment);
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
