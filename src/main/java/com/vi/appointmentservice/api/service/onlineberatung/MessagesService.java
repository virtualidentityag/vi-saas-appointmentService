package com.vi.appointmentservice.api.service.onlineberatung;

import com.vi.appointmentservice.adapters.keycloak.dto.KeycloakLoginResponseDTO;
import com.vi.appointmentservice.api.exception.httpresponses.NotFoundException;
import com.vi.appointmentservice.api.model.CalcomBooking;
import com.vi.appointmentservice.api.service.securityheader.SecurityHeaderSupplier;
import com.vi.appointmentservice.messageservice.generated.web.MessageControllerApi;
import com.vi.appointmentservice.messageservice.generated.web.model.AliasMessageDTO;
import com.vi.appointmentservice.messageservice.generated.web.model.MessageType;
import com.vi.appointmentservice.model.CalcomBookingToAsker;
import com.vi.appointmentservice.model.CalcomUserToConsultant;
import com.vi.appointmentservice.port.out.IdentityClient;
import com.vi.appointmentservice.repository.CalcomBookingToAskerRepository;
import com.vi.appointmentservice.repository.CalcomUserToConsultantRepository;
import com.vi.appointmentservice.api.service.calcom.CalComBookingService;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class MessagesService {

  private final @NonNull MessageControllerApi messageControllerApi;
  private final @NonNull UserService userService;
  private final @NonNull CalComBookingService calComBookingService;
  private final @NonNull CalcomUserToConsultantRepository calcomUserToConsultantRepository;
  private final @NonNull CalcomBookingToAskerRepository calcomBookingToAskerRepository;
  private final @NonNull IdentityClient identityClient;
  private final @NonNull SecurityHeaderSupplier securityHeaderSupplier;

  private final static SimpleDateFormat fromFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
  private final static SimpleDateFormat toFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
  private final static SimpleDateFormat toFormatMinutesOnly = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");

  @Value("${keycloakService.technical.username}")
  private String keycloakTechnicalUsername;

  @Value("${keycloakService.technical.password}")
  private String keycloakTechnicalPassword;

  private static String formatDate(String dateString){
    try {
      return toFormat.format(fromFormat.parse(dateString));
    } catch (ParseException e) {
      throw new RuntimeException(e);
    }
  }

  private static String formatDateWithoutSeconds(String dateString){
    try {
      return toFormatMinutesOnly.format(fromFormat.parse(dateString));
    } catch (ParseException e) {
      throw new RuntimeException(e);
    }
  }

  public void publishCancellationMessage(Long bookingId) {
    CalcomBooking booking = calComBookingService.getBookingById(bookingId);
    AliasMessageDTO message = createCancellationMessage(booking);
    sendMessage(booking, message);
  }

  public void publishNewAppointmentMessage(Long bookingId) {
    CalcomBooking booking = calComBookingService.getBookingById(bookingId);
    AliasMessageDTO message = createNewAppointmentMessage(booking);
    sendMessage(booking, message);
  }

  public void publishRescheduledAppointmentMessage(Long bookingId) {
    CalcomBooking booking = calComBookingService.getBookingById(bookingId);
    AliasMessageDTO message = createRescheduleAppointmentMessage(booking);
    sendMessage(booking, message);
  }

  private AliasMessageDTO createRescheduleAppointmentMessage(CalcomBooking booking) {
    AliasMessageDTO message = new AliasMessageDTO();
    JSONObject messageContent = new JSONObject();
    messageContent.put("title", booking.getTitle());
    message.setMessageType(MessageType.APPOINTMENT_RESCHEDULED);
    messageContent.put("date", LocalDateTime.parse(formatDate(booking.getStartTime())));
    messageContent.put("duration", ChronoUnit.MINUTES.between(
        LocalDateTime.parse(formatDateWithoutSeconds(booking.getStartTime())),
        LocalDateTime.parse(formatDateWithoutSeconds(booking.getEndTime()))));
    message.setContent(messageContent.toString());
    return message;
  }

  private AliasMessageDTO createNewAppointmentMessage(CalcomBooking booking) {
    AliasMessageDTO message = new AliasMessageDTO();
    JSONObject messageContent = new JSONObject();
    messageContent.put("title", booking.getTitle());
    message.setMessageType(MessageType.APPOINTMENT_SET);
    messageContent.put("date", LocalDateTime.parse(formatDate(booking.getStartTime())));
    messageContent.put("duration", ChronoUnit.MINUTES.between(
        LocalDateTime.parse(formatDateWithoutSeconds(booking.getStartTime())),
        LocalDateTime.parse(formatDateWithoutSeconds(booking.getEndTime()))));
    message.setContent(messageContent.toString());
    return message;
  }

  private AliasMessageDTO createCancellationMessage(CalcomBooking booking) {
    AliasMessageDTO message = new AliasMessageDTO();
    JSONObject messageContent = new JSONObject();
    messageContent.put("title", booking.getTitle());
    messageContent.put("date", LocalDateTime.parse(formatDate(booking.getStartTime())));
    messageContent.put("duration", ChronoUnit.MINUTES.between(
        LocalDateTime.parse(formatDateWithoutSeconds(booking.getStartTime())),
        LocalDateTime.parse(formatDateWithoutSeconds(booking.getEndTime()))));
    message.setMessageType(MessageType.APPOINTMENT_CANCELLED);
    message.setContent(messageContent.toString());
    return message;
  }

  private void sendMessage(CalcomBooking booking, AliasMessageDTO message) {
    addTechnicalUserHeaders(messageControllerApi.getApiClient());
    messageControllerApi.saveAliasMessageWithContent(getRocketChatGroupId(booking), message);
  }

  private String getRocketChatGroupId(CalcomBooking booking) {
    Optional<CalcomUserToConsultant> calcomUserToConsultant = calcomUserToConsultantRepository
        .findByCalComUserId(Long.valueOf(booking.getUserId()));
    if(calcomUserToConsultant.isPresent()){
      String consultantId = calcomUserToConsultant.get().getConsultantId();
      CalcomBookingToAsker byCalcomBookingId = calcomBookingToAskerRepository
          .findByCalcomBookingId(booking.getId());
      String askerId = byCalcomBookingId.getAskerId();
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


}
