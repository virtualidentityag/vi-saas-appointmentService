package com.vi.appointmentservice.api.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vi.appointmentservice.api.exception.httpresponses.BadRequestException;
import com.vi.appointmentservice.api.exception.httpresponses.InternalServerErrorException;
import com.vi.appointmentservice.api.facade.AskerFacade;
import com.vi.appointmentservice.api.model.CalcomBooking;
import com.vi.appointmentservice.api.model.CalcomWebhookInput;
import com.vi.appointmentservice.api.service.CalcomWebhookHandlerService;
import com.vi.appointmentservice.generated.api.controller.AskersApi;
import com.vi.appointmentservice.helper.AuthenticatedUser;
import com.vi.appointmentservice.helper.HmacHelper;
import io.swagger.annotations.Api;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
  private final @NonNull HmacHelper hmacHelper;
  private final @NonNull AuthenticatedUser authenticatedUser;

  @Value("${calcom.webhook.secret}")
  private String webhookSecret;

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
  public ResponseEntity<String> processBooking(String body, String xCalSignature256) {
    String verifyHmac = null;
    try {
      verifyHmac = hmacHelper.generateHmac("HmacSHA256", body, webhookSecret);
    } catch (Exception e) {
      throw new InternalServerErrorException(
          "Could not generate hmac for webhook verification: " + e);
    }
    if (xCalSignature256.equals(verifyHmac)) {
      ObjectMapper mapper = new ObjectMapper().configure(
          DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
      try {
        calcomWebhookHandlerService.handlePayload(mapper.readValue(body, CalcomWebhookInput.class));
      } catch (JsonProcessingException e) {
        throw new BadRequestException("Bad webhook body format.");
      }
      return new ResponseEntity<>(HttpStatus.OK);
    } else {
      throw new BadRequestException("Webhook secret could not be verifierd.");
    }
  }


}
