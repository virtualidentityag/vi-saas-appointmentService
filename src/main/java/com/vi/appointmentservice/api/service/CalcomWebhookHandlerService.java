package com.vi.appointmentservice.api.service;

import com.vi.appointmentservice.api.model.CalcomBooking;
import com.vi.appointmentservice.api.model.CalcomEventTypeDTO;
import com.vi.appointmentservice.api.model.CalcomWebhookInput;
import com.vi.appointmentservice.api.model.CalcomWebhookInputPayload;
import com.vi.appointmentservice.api.service.calcom.CalComBookingService;
import com.vi.appointmentservice.api.service.calcom.CalComEventTypeService;
import com.vi.appointmentservice.api.service.onlineberatung.MessagesService;
import com.vi.appointmentservice.model.CalcomBookingToAsker;
import com.vi.appointmentservice.repository.CalcomBookingToAskerRepository;
import com.vi.appointmentservice.repository.CalcomRepository;
import java.util.stream.Collectors;
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
  private final @NonNull CalComEventTypeService calComEventTypeService;
  private final @NonNull CalcomRepository calcomRepository;

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
  }

  private void handleCreateEvent(CalcomWebhookInputPayload payload) {
    createBookingAskerRelation(payload);
    if (!isTeamEvent(payload)) {
      messagesService.publishNewAppointmentMessage(Long.valueOf(payload.getBookingId()));
    }
  }

  private boolean isTeamEvent(CalcomWebhookInputPayload payload) {
    CalcomBooking booking = calComBookingService
        .getBookingById(Long.valueOf(payload.getBookingId()));
    CalcomEventTypeDTO eventType = calComEventTypeService
        .getEventTypeById(Long.valueOf(booking.getEventTypeId()));
    return eventType.getTeamId() != null;
  }

  private void handleRescheduleEvent(CalcomWebhookInputPayload payload) {
    calcomBookingToAskerRepository
        .deleteByCalcomBookingId(payload.getMetadata().getBookingId());
    String askerId = payload.getMetadata().getUser();
    Long newBookingId = Long.valueOf(payload.getBookingId());
    CalcomBookingToAsker userAssociation = new CalcomBookingToAsker(newBookingId, askerId);
    calcomBookingToAskerRepository.save(userAssociation);
    messagesService.publishRescheduledAppointmentMessage(newBookingId);
  }

  private void handleCancelEvent(CalcomWebhookInputPayload payload) {
    //TODO: replace with call to DB, and try catch will also disappear
    try {
      var bookingId = calComBookingService.getAllBookings().stream()
          .filter(el -> el.getUid().equals(payload.getUid())).collect(
              Collectors.toList()).get(0).getId();
      messagesService.publishCancellationMessage(bookingId);
      calcomBookingToAskerRepository.deleteByCalcomBookingId(bookingId);
      calcomRepository.cancelBookingById(bookingId);
    } catch (Exception e) {
      log.error(String.valueOf(e));
    }
  }

  private void createBookingAskerRelation(CalcomWebhookInputPayload payload) {
    var newBookingId = Long.valueOf(payload.getBookingId());
    String askerId = payload.getMetadata().getUser();
    CalcomBookingToAsker calcomBookingToAskerEntity = new CalcomBookingToAsker(newBookingId,
        askerId);
    calcomBookingToAskerRepository.save(calcomBookingToAskerEntity);
  }


}
