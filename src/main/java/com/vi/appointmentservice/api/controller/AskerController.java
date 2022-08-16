package com.vi.appointmentservice.api.controller;

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
  public ResponseEntity<List<CalcomBooking>> getAllBookingsOfAsker(String askerId) {
    List<CalcomBooking> bookings;
    bookings = askerFacade.getAskerActiveBookings(askerId);
    return new ResponseEntity<>(bookings, HttpStatus.OK);
  }

  @Override
  public ResponseEntity<String> processBooking(CalcomWebhookInput input) {
    //TODO: has been moved to a separate controller, this can be removed once DB data with the old webhook links has been completely replaced
    calcomWebhookHandlerService.handlePayload(input);
    return new ResponseEntity<>(HttpStatus.OK);
  }


}
