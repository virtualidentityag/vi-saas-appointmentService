package com.vi.appointmentservice.api.service.onlineberatung;

import com.vi.appointmentservice.adapters.keycloak.dto.KeycloakLoginResponseDTO;
import com.vi.appointmentservice.api.service.securityheader.SecurityHeaderSupplier;
import com.vi.appointmentservice.appointmentservice.generated.ApiClient;
import com.vi.appointmentservice.appointmentservice.generated.web.AppointmentControllerApi;
import com.vi.appointmentservice.appointmentservice.generated.web.model.Appointment;
import com.vi.appointmentservice.appointmentservice.generated.web.model.AppointmentStatus;
import com.vi.appointmentservice.config.VideoAppointmentsApiClient;
import com.vi.appointmentservice.port.out.IdentityClient;
import java.time.OffsetDateTime;
import java.util.Optional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
@RequiredArgsConstructor
public class VideoAppointmentService {

  private final @NonNull IdentityClient identityClient;
  private final @NonNull SecurityHeaderSupplier securityHeaderSupplier;

  @Value("${keycloakService.technical.username}")
  private String keycloakTechnicalUsername;

  @Value("${keycloakService.technical.password}")
  private String keycloakTechnicalPassword;

  @Value("${user.service.api.url}")
  private String videoAppointmentServiceApiUrl;


  public Optional<Appointment> findAppointmentByBookingId(Integer bookingId) {
    var appointmentControllerApi = getAppointmentControllerApi();
    addTechnicalUserHeaders(appointmentControllerApi.getApiClient());
    try {
      return Optional.of(appointmentControllerApi.getAppointmentByBookingId(bookingId));
    } catch (HttpClientErrorException.NotFound e) {
      log.debug("Appointment not found for bookingId: {}", bookingId);
      return Optional.empty();
    } catch (Exception e) {
      log.error("Error while fetching appointment by bookingId: {}", bookingId, e);
      return Optional.empty();
    }
  }

  public Appointment createAppointment(String consultantEmail, OffsetDateTime startTime, Integer bookingId) {
    Appointment appointment = new Appointment();
    appointment.setConsultantEmail(consultantEmail);
    appointment.setStatus(AppointmentStatus.CREATED);
    appointment.setDatetime(startTime);
    appointment.setBookingId(bookingId);
    var appointmentControllerApi = getAppointmentControllerApi();
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


  public AppointmentControllerApi getAppointmentControllerApi() {
    final RestTemplate restTemplate = new RestTemplate();
    final HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
    final HttpClient httpClient = HttpClientBuilder.create()
        .setRedirectStrategy(new LaxRedirectStrategy())
        .build();
    factory.setHttpClient(httpClient);
    restTemplate.setRequestFactory(factory);
    ApiClient apiClient = new VideoAppointmentsApiClient(restTemplate);
    apiClient.setBasePath(this.videoAppointmentServiceApiUrl);
    return new AppointmentControllerApi(apiClient);
  }
}
