package com.vi.appointmentservice.api.controller;

import com.vi.appointmentservice.api.exception.httpresponses.BadRequestException;
import com.vi.appointmentservice.api.exception.httpresponses.InternalServerErrorException;
import com.vi.appointmentservice.api.model.CalcomWebhookInput;
import com.vi.appointmentservice.api.service.CalcomWebhookHandlerService;
import com.vi.appointmentservice.generated.api.controller.ProcessBookingApi;
import com.vi.appointmentservice.helper.HmacHelper;
import io.swagger.annotations.Api;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Api(tags = "booking")
@Slf4j
@RequiredArgsConstructor
public class BookingController implements ProcessBookingApi {

  private final @NonNull CalcomWebhookHandlerService calcomWebhookHandlerService;

  private final @NonNull HmacHelper hmacHelper;
  @Value("${calcom.webhook.secret}")
  private String webhookSecret;

  @Override
  public ResponseEntity<String> processBookingWebhook(CalcomWebhookInput input,
      String xCalSignature256) {
    try {
      if (xCalSignature256.equals(hmacHelper.generateHmac("HmacSHA256", input.toString(), webhookSecret))) {
        calcomWebhookHandlerService.handlePayload(input);
        return new ResponseEntity<>(HttpStatus.OK);
      } else {
        throw new BadRequestException("Webhook secret could not be verifierd.");
      }
    } catch (Exception e) {
      throw new InternalServerErrorException("Could not genereate hmac for webhook verification.");
    }
  }
}
