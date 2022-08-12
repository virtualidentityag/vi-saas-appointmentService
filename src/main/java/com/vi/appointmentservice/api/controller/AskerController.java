package com.vi.appointmentservice.api.controller;

import com.vi.appointmentservice.api.exception.httpresponses.BadRequestException;
import com.vi.appointmentservice.api.facade.AskerFacade;
import com.vi.appointmentservice.api.model.CalcomBooking;
import com.vi.appointmentservice.api.model.CalcomWebhookInput;
import com.vi.appointmentservice.api.service.CalcomWebhookHandlerService;
import com.vi.appointmentservice.generated.api.controller.AskersApi;
import io.swagger.annotations.Api;
import java.util.List;
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
  private final @NonNull CalcomWebhookHandlerService calcomWebhookHandlerService;
  private final @NonNull AskerFacade askerFacade;

  @Override
  public ResponseEntity<List<CalcomBooking>> getAllBookingsOfAsker(String askerId, String status) {
    List<CalcomBooking> bookings;
    if(status == null){
      return new ResponseEntity<>(askerFacade.getAllBookingsOfAskerHandler(askerId), HttpStatus.OK);
    } else if ("ACTIVE".equals(status)) {
      bookings = askerFacade.getAskerActiveBookings(askerId);
    } else if ("EXPIRED".equals(status)) {
      bookings = askerFacade.getAskerExpiredBookings(askerId);
    } else if ("CANCELLED".equals(status)) {
      bookings = askerFacade.getAskerCancelledBookings(askerId);
    } else {
      throw new BadRequestException("Given status must be ACTIVE, EXPIRED or CANCELLED");
    }
    return new ResponseEntity<>(bookings,
        HttpStatus.OK);
  }

  @Override
  public ResponseEntity<String> processBooking(CalcomWebhookInput input) {
    //TODO: move this to a separate controller, has nothing to do with askers
    calcomWebhookHandlerService.handlePayload(input);
    return new ResponseEntity<>(HttpStatus.OK);
  }


}
