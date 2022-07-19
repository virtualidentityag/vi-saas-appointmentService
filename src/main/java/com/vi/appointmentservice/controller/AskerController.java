package com.vi.appointmentservice.controller;

import com.vi.appointmentservice.api.model.CalcomBooking;
import com.vi.appointmentservice.api.model.CalcomWebhookInput;
import com.vi.appointmentservice.api.model.MeetingSlug;
import com.vi.appointmentservice.generated.api.controller.AskersApi;
import com.vi.appointmentservice.helper.RescheduleHelper;
import com.vi.appointmentservice.model.CalcomBookingToAsker;
import com.vi.appointmentservice.repository.CalcomBookingToAskerRepository;
import com.vi.appointmentservice.service.calcom.CalComBookingService;
import com.vi.appointmentservice.repository.CalcomRepository;
import com.vi.appointmentservice.service.CalcomWebhookHandlerService;
import io.swagger.annotations.Api;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;


@RestController
@Api(tags = "asker")
@Slf4j
@RequiredArgsConstructor
public class AskerController implements AskersApi {

  private final @NonNull RescheduleHelper rescheduleHelper;
  private final @NonNull CalcomBookingToAskerRepository calcomBookingToAskerRepository;
  private final @NonNull CalcomRepository calcomRepository;
  private final @NonNull CalcomWebhookHandlerService calcomWebhookHandlerService;
  private final @NonNull CalComBookingService calComBookingService;

  @Override
  public ResponseEntity<List<CalcomBooking>> getAllBookingsOfAsker(String askerId) {
    //TODO: bad naming getAllBookingsOfAsker. we are already in asker context.
    // give it a name like getBookings. we will than have the same method in ConsultantsController

    //TODO: get rid of try catch here
    try {

      // TODO: try something like this, to avoid nesting of code
      //      if (calcomBookingToAskerRepository.existsByAskerId(askerId)) {
      //        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
      //      }

      if (calcomBookingToAskerRepository.existsByAskerId(askerId)) {
        List<CalcomBookingToAsker> bookingIds = calcomBookingToAskerRepository
            .findByAskerId(askerId);
        List<CalcomBooking> bookings = calcomRepository
            .getByIds(bookingIds.stream().map(el -> el.getCalcomBookingId()).collect(
                Collectors.toList()));
        for (CalcomBooking booking : bookings) {
          //TODO: this part with the lines above should be moved to service layer
          rescheduleHelper.attachRescheduleLink(booking);
          rescheduleHelper.attachConsultantName(booking);
        }

        return new ResponseEntity<>(bookings, HttpStatus.OK);
      } else {
        //TODO: before we had HttpStatus.NOT_FOUND as a response. no need for that.
        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.OK);
      }
    } catch (Exception e) {
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @Override
  public ResponseEntity<CalcomBooking> getBookingDetails(String bookingId) {
    //TODO: where do we use this?
    // in case we don't use it, please remove it
    try {
      CalcomBooking booking = calComBookingService.getBookingById(Long.valueOf(bookingId));
      if (booking != null) {
        rescheduleHelper.attachRescheduleLink(booking);
        rescheduleHelper.attachConsultantName(booking);
        return new ResponseEntity<>(booking, HttpStatus.OK);
      } else {
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
      }

    } catch (Exception e) {
      return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @Override
  public ResponseEntity<MeetingSlug> getAskerMeetingSlug(String askerId) {
    //TODO: where do we use this?
    // in case we don't use it, please remove it
    return AskersApi.super.getAskerMeetingSlug(askerId);
  }

  @Override
  public ResponseEntity<String> processBooking(CalcomWebhookInput input) {
    //TODO: move this to a separate controller, has nothing to do with askers
    calcomWebhookHandlerService.handlePayload(input);
    return new ResponseEntity<>(HttpStatus.OK);
  }


}
