package com.vi.appointmentservice.api.controller;

import com.vi.appointmentservice.api.exception.httpresponses.BadRequestException;
import com.vi.appointmentservice.api.facade.ConsultantFacade;
import com.vi.appointmentservice.api.model.CalcomBooking;
import com.vi.appointmentservice.api.model.CalcomEventTypeDTO;
import com.vi.appointmentservice.api.model.CalcomToken;
import com.vi.appointmentservice.api.model.CalcomUser;
import com.vi.appointmentservice.api.model.ConsultantDTO;
import com.vi.appointmentservice.api.model.MeetingSlug;
import com.vi.appointmentservice.generated.api.controller.ConsultantsApi;
import com.vi.appointmentservice.helper.AuthenticatedUser;
import io.swagger.annotations.Api;
import java.util.List;
import java.util.Objects;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for consultant API operations.
 */
@RestController
@Api(tags = "consultant")
@Slf4j
@RequiredArgsConstructor
public class ConsultantController implements ConsultantsApi {

  private final @NonNull AuthenticatedUser authenticatedUser;
  private final @NonNull ConsultantFacade consultantFacade;


  @GetMapping(value = "/consultants/initialize", produces = {"application/json"})
  ResponseEntity<String> initializeConsultants() {
    return new ResponseEntity<>(this.consultantFacade.initializeConsultantsHandler().toString(),
        HttpStatus.OK);
  }

  @Override
  public ResponseEntity<CalcomUser> createConsultant(ConsultantDTO consultant) {
    return new ResponseEntity<>(this.consultantFacade.createOrUpdateCalcomUserHandler(consultant),
        HttpStatus.OK);
  }

  @Override
  public ResponseEntity<Void> deleteConsultant(String consultantId) {
    return new ResponseEntity<>(this.consultantFacade.deleteConsultantHandler(consultantId));
  }

  @Override
  public ResponseEntity<CalcomUser> updateConsultant(String consultantId,
      ConsultantDTO consultant) {
    if (Objects.equals(consultantId, consultant.getId())) {
      return new ResponseEntity<>(this.consultantFacade.createOrUpdateCalcomUserHandler(consultant),
          HttpStatus.OK);
    } else {
      throw new BadRequestException(
          String.format("Route consultantId '%s' and user id '%s' from user object dont match",
              consultantId, consultant.getId()));
    }
  }

  @Override
  public ResponseEntity<List<CalcomBooking>> getAllBookingsOfConsultant(String consultantId,
      String status) {
    if (authenticatedUser.getUserId().equals(consultantId)) {
      List<CalcomBooking> bookings;
      if ("ACTIVE".equals(status)) {
        bookings = consultantFacade.getConsultantActiveBookings(consultantId);
      } else if ("EXPIRED".equals(status)) {
        bookings = consultantFacade.getConsultantExpiredBookings(consultantId);
      } else if ("CANCELLED".equals(status)) {
        bookings = consultantFacade.getConsultantCancelledBookings(consultantId);
      } else {
        throw new BadRequestException("Given status must be ACTIVE, EXPIRED or CANCELLED");
      }
      return new ResponseEntity<>(bookings,
          HttpStatus.OK);
    } else {
      throw new BadRequestException("Not authorized for given consultantId");
    }
  }

  @Override
  public ResponseEntity<List<CalcomEventTypeDTO>> getAllEventTypesOfConsultant(
      String consultantId) {
    return new ResponseEntity<>(consultantFacade.getAllEventTypesOfConsultantHandler(consultantId),
        HttpStatus.OK);
  }

  @Override
  public ResponseEntity<MeetingSlug> getConsultantMeetingSlug(String consultantId) {
    return new ResponseEntity<>(consultantFacade.getConsultantMeetingSlugHandler(consultantId),
        HttpStatus.OK);
  }

  @Override
  public ResponseEntity<CalcomToken> getToken() {
    return new ResponseEntity<>(consultantFacade.getToken(authenticatedUser.getUserId()), HttpStatus.OK);
  }
}
