package com.vi.appointmentservice.api.service.onlineberatung;

import com.vi.appointmentservice.adapters.keycloak.dto.KeycloakLoginResponseDTO;
import com.vi.appointmentservice.api.exception.httpresponses.NotFoundException;
import com.vi.appointmentservice.api.model.CalcomBooking;
import com.vi.appointmentservice.api.calcom.service.CalComBookingService;
import com.vi.appointmentservice.api.service.securityheader.SecurityHeaderSupplier;
import com.vi.appointmentservice.config.MessageApiClient;
import com.vi.appointmentservice.messageservice.generated.web.MessageControllerApi;
import com.vi.appointmentservice.messageservice.generated.web.model.AliasMessageDTO;
import com.vi.appointmentservice.messageservice.generated.web.model.MessageType;
import com.vi.appointmentservice.model.CalcomBookingToAsker;
import com.vi.appointmentservice.model.CalcomUserToConsultant;
import com.vi.appointmentservice.port.out.IdentityClient;
import com.vi.appointmentservice.repository.CalcomBookingToAskerRepository;
import com.vi.appointmentservice.repository.UserToConsultantRepository;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
@RequiredArgsConstructor
public class MessagesService {

  private static final int MAX_ATTEMPTS = 15;
  private final @NonNull UserService userService;
  private final @NonNull CalComBookingService calComBookingService;
  private final @NonNull UserToConsultantRepository userToConsultantRepository;
  private final @NonNull CalcomBookingToAskerRepository calcomBookingToAskerRepository;
  private final @NonNull IdentityClient identityClient;
  private final @NonNull SecurityHeaderSupplier securityHeaderSupplier;
  private final SimpleDateFormat toFormatMinutesOnly = new SimpleDateFormat(
      "yyyy-MM-dd'T'HH:mm");
  private final SimpleDateFormat toFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
  @Value("${message.service.api.url}")
  private String messageServiceApiUrl;

  @Value("${keycloakService.technical.username}")
  private String keycloakTechnicalUsername;

  @Value("${keycloakService.technical.password}")
  private String keycloakTechnicalPassword;

  private String formatDate(String dateString) {
    try {
      return toFormat.format(toFormatMinutesOnly.parse(dateString));
    } catch (ParseException e) {
      throw new IllegalArgumentException(e);
    }
  }

  public void publishCancellationMessage(String bookingUid, String cancellationMessage) {
    CalcomBooking booking = calComBookingService.getBookingByUid(bookingUid);
    // in case of cancellation event, the message is not saved to booking at the time this handler is called
    // like it is done for creation and rescheduling
    booking.setCancellationReason(cancellationMessage);
    AliasMessageDTO message = createMessage(booking, MessageType.APPOINTMENT_CANCELLED);
    sendMessage(booking, message);
  }

  @Async
  public void publishNewAppointmentMessage(Long bookingId) {
      new BookingCreationRepeater(MAX_ATTEMPTS).tryRepeatCreateMessage(bookingId).orElseLogError();
  }

  @Data
  class BookingCreationRepeater {

    int attemptsLeft;
    private static final int INTERVAL_BETWEEN_CALLS = 2000;
    private boolean messageSuccessfullyCreated;

    public BookingCreationRepeater(int maxAttempts) {
      attemptsLeft = maxAttempts;
      messageSuccessfullyCreated = false;
    }

    public BookingCreationRepeater tryRepeatCreateMessage(Long bookingId) {
      while (!messageSuccessfullyCreated(bookingId) && attemptsLeft > 0) {
        sleepGivenInterval();
        attemptsLeft--;
      }
      return this;
    }

    private void sleepGivenInterval() {
      try {
        Thread.sleep(INTERVAL_BETWEEN_CALLS);
      } catch (InterruptedException e) {
        throw new IllegalStateException(e);
      }
    }

    private boolean messageSuccessfullyCreated(Long bookingId) {
      try {
        CalcomBooking booking = calComBookingService.getBookingById(bookingId);
        AliasMessageDTO message = createMessage(booking, MessageType.APPOINTMENT_SET);
        sendMessage(booking, message);
        this.messageSuccessfullyCreated = true;
        log.info("Successfully published new appointmentMessage for bookingId {}", bookingId);
        return true;
      } catch (Exception e) {
        this.messageSuccessfullyCreated = false;
        return false;
      }
    }

    public void orElseLogError() {
      if (!messageSuccessfullyCreated) {
        log.error("Unable to publish new appointmentMessage. Reason: max attempts failed");
      }
    }
  }

  public void publishRescheduledAppointmentMessage(Long bookingId) {
    CalcomBooking booking = calComBookingService.getBookingById(bookingId);
    AliasMessageDTO message = createMessage(booking, MessageType.APPOINTMENT_RESCHEDULED);
    sendMessage(booking, message);
  }

  private AliasMessageDTO createMessage(CalcomBooking booking, MessageType messageType) {
    AliasMessageDTO message = new AliasMessageDTO();
    JSONObject messageContent = new JSONObject();
    messageContent.put("title", booking.getTitle());
    message.setMessageType(messageType);
    messageContent.put("date", LocalDateTime.parse(formatDate(booking.getStartTime())));
    messageContent.put("duration", ChronoUnit.MINUTES.between(
        LocalDateTime.parse(formatDate(booking.getStartTime())),
        LocalDateTime.parse(formatDate(booking.getEndTime()))));
    if(messageType.equals(MessageType.APPOINTMENT_SET)){
      messageContent.put("note", booking.getDescription());
    }else{
      messageContent.put("note", booking.getCancellationReason());
    }
    message.setContent(messageContent.toString());
    return message;
  }

  private void sendMessage(CalcomBooking booking, AliasMessageDTO message) {
    var messageControllerApi = getMessageControllerApi();
    addTechnicalUserHeaders(messageControllerApi.getApiClient());
    messageControllerApi.saveAliasMessageWithContent(getRocketChatGroupId(booking), message);
  }

  private String getRocketChatGroupId(CalcomBooking booking) {
    Optional<CalcomUserToConsultant> calcomUserToConsultant = userToConsultantRepository
        .findByCalComUserId(Long.valueOf(booking.getUserId()));
    if (calcomUserToConsultant.isPresent()) {
      String consultantId = calcomUserToConsultant.get().getConsultantId();
      Optional<CalcomBookingToAsker> byCalcomBookingId = calcomBookingToAskerRepository
          .findByCalcomBookingId(booking.getId());
      String askerId = byCalcomBookingId.orElseThrow().getAskerId();
      return userService
          .getRocketChatGroupId(consultantId, askerId);
    }
    throw new NotFoundException("No consultant found for calcom user in booking");
  }

  private void addTechnicalUserHeaders(
      com.vi.appointmentservice.messageservice.generated.ApiClient apiClient) {
    KeycloakLoginResponseDTO keycloakLoginResponseDTO = identityClient.loginUser(
        keycloakTechnicalUsername, keycloakTechnicalPassword
    );
    log.debug("Technical Acces Token: {}", keycloakLoginResponseDTO.getAccessToken());
    HttpHeaders headers = this.securityHeaderSupplier
        .getKeycloakAndCsrfHttpHeaders(keycloakLoginResponseDTO.getAccessToken());
    headers.forEach((key, value) -> apiClient.addDefaultHeader(key, value.iterator().next()));
  }

  public MessageControllerApi getMessageControllerApi() {
    final RestTemplate restTemplate = new RestTemplate();
    final HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
    final HttpClient httpClient = HttpClientBuilder.create()
        .setRedirectStrategy(new LaxRedirectStrategy())
        .build();
    factory.setHttpClient(httpClient);
    restTemplate.setRequestFactory(factory);
    com.vi.appointmentservice.messageservice.generated.ApiClient apiClient = new MessageApiClient(
        restTemplate);
    apiClient.setBasePath(this.messageServiceApiUrl);
    return new MessageControllerApi(apiClient);
  }

}
