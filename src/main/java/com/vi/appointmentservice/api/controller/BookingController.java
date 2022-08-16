package com.vi.appointmentservice.api.controller;

import com.vi.appointmentservice.api.model.CalcomWebhookInput;
import com.vi.appointmentservice.api.service.CalcomWebhookHandlerService;
import com.vi.appointmentservice.generated.api.controller.ProcessBookingApi;
import io.swagger.annotations.Api;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Api(tags = "booking")
@Slf4j
@RequiredArgsConstructor
public class BookingController implements ProcessBookingApi {

  private final @NonNull CalcomWebhookHandlerService calcomWebhookHandlerService;

  @Override
  public ResponseEntity<String> processBookingWebhook(CalcomWebhookInput input) {
    calcomWebhookHandlerService.handlePayload(input);
    return new ResponseEntity<>(HttpStatus.OK);
  }
}
