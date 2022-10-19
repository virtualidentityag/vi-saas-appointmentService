package com.vi.appointmentservice.api.controller;

import com.vi.appointmentservice.api.exception.httpresponses.BadRequestException;
import com.vi.appointmentservice.api.facade.AskerFacade;
import com.vi.appointmentservice.api.model.CalcomBooking;
import com.vi.appointmentservice.api.service.CalcomWebhookHandlerService;
import com.vi.appointmentservice.generated.api.controller.AskersApi;
import com.vi.appointmentservice.helper.AuthenticatedUser;
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
  private final @NonNull AuthenticatedUser authenticatedUser;

  @Override
  public ResponseEntity<List<CalcomBooking>> getAllBookingsOfAsker(String askerId) {
    if (authenticatedUser.getUserId().equals(askerId)) {
      List<CalcomBooking> bookings;
      bookings = askerFacade.getAskerActiveBookings(askerId);
      return new ResponseEntity<>(bookings, HttpStatus.OK);
    } else {
      throw new BadRequestException("Not authorized for given askerId");
    }
  }

  @Override
  public ResponseEntity<Void> deleteAskerData(String askerId) {
    askerFacade.deleteAskerData(askerId);
    return new ResponseEntity<Void>(HttpStatus.OK);
  }
}
