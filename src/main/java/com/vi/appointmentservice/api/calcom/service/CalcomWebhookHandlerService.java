package com.vi.appointmentservice.api.calcom.service;

import com.vi.appointmentservice.api.calcom.repository.BookingRepository;
import com.vi.appointmentservice.api.exception.httpresponses.InternalServerErrorException;
import com.vi.appointmentservice.api.model.CalcomBooking;
import com.vi.appointmentservice.api.model.CalcomWebhookInput;
import com.vi.appointmentservice.api.model.CalcomWebhookInputPayload;
import com.vi.appointmentservice.api.calcom.service.CalComBookingService;
import com.vi.appointmentservice.api.service.onlineberatung.AdminUserService;
import com.vi.appointmentservice.api.service.onlineberatung.MessagesService;
import com.vi.appointmentservice.api.service.onlineberatung.VideoAppointmentService;
import com.vi.appointmentservice.api.service.statistics.StatisticsService;
import com.vi.appointmentservice.api.service.statistics.event.BookingCanceledStatisticsEvent;
import com.vi.appointmentservice.api.service.statistics.event.BookingCreatedStatisticsEvent;
import com.vi.appointmentservice.api.service.statistics.event.BookingRescheduledStatisticsEvent;
import com.vi.appointmentservice.appointmentservice.generated.web.model.Appointment;
import com.vi.appointmentservice.model.CalcomBookingToAsker;
import com.vi.appointmentservice.model.CalcomUserToConsultant;
import com.vi.appointmentservice.repository.CalcomBookingToAskerRepository;
import com.vi.appointmentservice.repository.UserToConsultantRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CalcomWebhookHandlerService {

  private final @NonNull CalcomBookingToAskerRepository calcomBookingToAskerRepository;
  private final @NonNull MessagesService messagesService;
  private final @NonNull CalComBookingService calComBookingService;
  private final @NonNull VideoAppointmentService videoAppointmentService;
  private final @NonNull StatisticsService statisticsService;
  private final @NonNull UserToConsultantRepository userToConsultantRepository;
  private final @NonNull AdminUserService adminUserService;
  private final @NonNull BookingRepository bookingRepository;

  @Transactional
  public void handlePayload(CalcomWebhookInput input) {
    CalcomWebhookInputPayload payload = input.getPayload();

    if (payload == null) {
      log.warn("Payload of webhook is empty");
      return;
    }

    if ("BOOKING_CREATED".equals(input.getTriggerEvent())) {
      handleCreateEvent(payload);
    } else if ("BOOKING_RESCHEDULED".equals(input.getTriggerEvent())) {
      handleRescheduleEvent(payload);
    } else if ("BOOKING_CANCELLED".equals(input.getTriggerEvent())) {
      handleCancelEvent(payload);
    }
    try {
      createStatisticsEvent(input.getTriggerEvent(), payload);
    } catch (Exception e) {
      log.error("Could not create statistics event", e);
    }
  }

  private void handleCreateEvent(CalcomWebhookInputPayload payload) {
    Appointment appointment = videoAppointmentService
        .createAppointment(payload.getOrganizer().getEmail(), payload.getStartTime());
    createBookingAskerRelation(payload, appointment.getId());
    messagesService.publishNewAppointmentMessage(Long.valueOf(payload.getBookingId()));
  }

  private String getConsultantId(Integer bookingId) {
    CalcomBooking booking = calComBookingService.getBookingById(Long.valueOf(bookingId));
    Optional<CalcomUserToConsultant> calcomUserToConsultant = this.userToConsultantRepository
        .findByCalComUserId(
            Long.valueOf(booking.getUserId()));
    if (calcomUserToConsultant.isPresent()) {
      String consultantId = calcomUserToConsultant.get().getConsultantId();
      com.vi.appointmentservice.useradminservice.generated.web.model.ConsultantDTO consultant = null;
      consultant = this.adminUserService.getConsultantById(consultantId);
      return consultant.getId();
    } else {
      throw new InternalServerErrorException(
          "Could not find calcomUserToConsultant for bookingId " + bookingId);
    }
  }

  private void handleRescheduleEvent(CalcomWebhookInputPayload payload) {
    Appointment appointment = videoAppointmentService
        .createAppointment(payload.getOrganizer().getEmail(), payload.getStartTime());
    calcomBookingToAskerRepository
        .deleteByCalcomBookingId(payload.getMetadata().getBookingId());
    String askerId = payload.getMetadata().getUser();
    Long newBookingId = Long.valueOf(payload.getBookingId());
    CalcomBookingToAsker userAssociation = new CalcomBookingToAsker(newBookingId, askerId,
        appointment.getId().toString());
    calcomBookingToAskerRepository.save(userAssociation);
    messagesService.publishRescheduledAppointmentMessage(newBookingId);
  }

  private void handleCancelEvent(CalcomWebhookInputPayload payload) {
    messagesService.publishCancellationMessage(payload.getUid());
  }

  private void createBookingAskerRelation(CalcomWebhookInputPayload payload,
      UUID appointmentId) {
    var newBookingId = Long.valueOf(payload.getBookingId());
    String askerId = payload.getMetadata().getUser();
    CalcomBookingToAsker calcomBookingToAskerEntity = new CalcomBookingToAsker(newBookingId,
        askerId, appointmentId.toString());
    calcomBookingToAskerRepository.save(calcomBookingToAskerEntity);
  }

  private void createStatisticsEvent(String eventType, CalcomWebhookInputPayload payload) {
    switch (eventType) {
      case "BOOKING_CREATED":
        statisticsService.fireEvent(new BookingCreatedStatisticsEvent(payload,
            this.getConsultantId(payload.getBookingId())));
        break;
      case "BOOKING_RESCHEDULED":
        statisticsService.fireEvent(new BookingRescheduledStatisticsEvent(payload,
            this.getConsultantId(payload.getBookingId())));
        break;
      case "BOOKING_CANCELLED":
        Integer bookingId = bookingRepository.getBookingIdByUid(payload.getUid());
        statisticsService.fireEvent(
            new BookingCanceledStatisticsEvent(payload, this.getConsultantId(bookingId),
                bookingId));
        break;
      default:
        log.warn("Webhook event {} ignored for statistics", eventType);
    }
  }

}
