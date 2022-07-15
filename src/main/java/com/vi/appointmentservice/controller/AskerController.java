package com.vi.appointmentservice.controller;

import com.vi.appointmentservice.api.model.CalcomBooking;
import com.vi.appointmentservice.api.model.CalcomWebhook;
import com.vi.appointmentservice.api.model.CalcomWebhookPayload;
import com.vi.appointmentservice.api.model.MeetingSlug;
import com.vi.appointmentservice.generated.api.controller.AskersApi;
import com.vi.appointmentservice.helper.RescheduleHelper;
import com.vi.appointmentservice.model.CalcomBookingToAsker;
import com.vi.appointmentservice.repository.CalcomBookingToAskerRepository;
import com.vi.appointmentservice.service.CalComBookingService;
import com.vi.appointmentservice.service.MessagesService;
import io.swagger.annotations.Api;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;


@RestController
@Api(tags = "asker")
@Slf4j
@RequiredArgsConstructor
public class AskerController implements AskersApi {

  private final @NonNull CalComBookingService calComBookingService;
  private final @NonNull MessagesService messagesService;
  private final @NonNull RescheduleHelper rescheduleHelper;
  private final @NonNull CalcomBookingToAskerRepository calcomBookingToAskerRepository;


  @Override
  public ResponseEntity<List<CalcomBooking>> getAllBookingsOfAsker(String askerId) {
    try {
      List<CalcomBookingToAsker> bookingIds = calcomBookingToAskerRepository.findByAskerId(askerId);
      List<CalcomBooking> bookings = new ArrayList<>();

      for (CalcomBookingToAsker bookingId : bookingIds) {
        bookings.add(calComBookingService.getBookingById(bookingId.getCalcomBookingId()));
      }
      for (CalcomBooking booking : bookings) {
        rescheduleHelper.attachRescheduleLink(booking);
      }

      return new ResponseEntity<>(bookings, HttpStatus.OK);
    } catch (Exception e) {
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @Override
  public ResponseEntity<CalcomBooking> getBookingDetails(String bookingId) {
    try {
      CalcomBooking booking = calComBookingService.getBookingById(Long.valueOf(bookingId));
      return new ResponseEntity<>(rescheduleHelper.attachRescheduleLink(booking), HttpStatus.OK);
    } catch (Exception e) {
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @Override
  public ResponseEntity<MeetingSlug> getAskerMeetingSlug(String askerId) {
    return AskersApi.super.getAskerMeetingSlug(askerId);
  }

  @Override
  @Transactional
  public ResponseEntity<String> processBooking(CalcomWebhook calcomWebhook) {
    try {
      CalcomWebhookPayload payload = calcomWebhook.getPayload();
      Long bookingId = null;
      if (payload != null) {
        if (calcomWebhook.getTriggerEvent().equals("BOOKING_CREATED")) {
          bookingId = Long.valueOf(payload.getBookingId());
          String askerId = payload.getMetadata().getUser();
          CalcomBookingToAsker userAssociation = new CalcomBookingToAsker(bookingId, askerId);
          calcomBookingToAskerRepository.save(userAssociation);
          Boolean isInitialAppointment = payload.getMetadata().getIsInitialAppointment();
          if (isInitialAppointment == null || isInitialAppointment.equals(false)) {
            messagesService.publishNewAppointmentMessage(bookingId);
          }
        } else if (calcomWebhook.getTriggerEvent().equals("BOOKING_RESCHEDULED")) {
          String askerId = payload.getMetadata().getUser();
          Long newBookingId = Long.valueOf(payload.getBookingId());
          CalcomBookingToAsker userAssociation = new CalcomBookingToAsker(newBookingId, askerId);
          Long oldBookingId = payload.getMetadata().getBookingId();
          messagesService.publishCancellationMessage(oldBookingId);
          calcomBookingToAskerRepository.deleteByCalcomBookingId(oldBookingId);
          calcomBookingToAskerRepository.save(userAssociation);
          messagesService.publishNewAppointmentMessage(newBookingId);
        } else {
          //TODO: change this. we need to get booking id based on uuid or save it also in the relational
          // entity
          bookingId = Long.valueOf(calComBookingService.getAllBookings().stream()
              .filter(el -> el.getUid().equals(payload.getUid())).collect(
                  Collectors.toList()).get(0).getId());
          calcomBookingToAskerRepository.deleteByCalcomBookingId(bookingId);
          messagesService.publishCancellationMessage(bookingId);
        }

        return new ResponseEntity<>(String.valueOf(bookingId), HttpStatus.OK);
      } else {
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
      }
    } catch (Exception e) {
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }


}
