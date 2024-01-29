package com.vi.appointmentservice.api.controller;

import com.vi.appointmentservice.api.exception.httpresponses.BadRequestException;
import com.vi.appointmentservice.api.facade.ConsultantFacade;
import com.vi.appointmentservice.api.model.CalcomBooking;
import com.vi.appointmentservice.api.model.CalcomToken;
import com.vi.appointmentservice.api.model.ConsultantDTO;
import com.vi.appointmentservice.api.model.EventTypeDTO;
import com.vi.appointmentservice.api.model.MeetingSlug;
import com.vi.appointmentservice.api.model.PatchConsultantDTO;
import com.vi.appointmentservice.generated.api.controller.ConsultantsApi;
import com.vi.appointmentservice.helper.AuthenticatedUser;
import io.swagger.annotations.Api;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for consultant API operations.
 */
@RestController
@Api(tags = "consultant")
@RequiredArgsConstructor
public class ConsultantController implements ConsultantsApi {

  private final @NonNull AuthenticatedUser authenticatedUser;

  private final @NonNull ConsultantFacade consultantFacade;

  @Override
  public ResponseEntity<Void> createConsultant(ConsultantDTO consultant) {
    this.consultantFacade.createUser(consultant);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @Override
  public ResponseEntity<Void> deleteConsultant(String consultantId) {
    this.consultantFacade.deleteConsultantHandler(consultantId);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @Override
  public ResponseEntity<Void> updateConsultant(String consultantId,
      ConsultantDTO consultant) {
    this.consultantFacade.updateAppointmentUser(consultant);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @Override
  public ResponseEntity<Void> patchConsultant(String consultantId, PatchConsultantDTO consultant) {
    this.consultantFacade.patchAppointmentUser(consultantId, consultant);
    return new ResponseEntity<>(HttpStatus.OK);
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
  public ResponseEntity<List<EventTypeDTO>> getAllEventTypesOfConsultant(
      String consultantId) {
    //TODO: remove this method
    return null;
  }

  @Override
  public ResponseEntity<MeetingSlug> getConsultantMeetingSlug(String consultantId) {
    return new ResponseEntity<>(consultantFacade.getConsultantMeetingSlugHandler(consultantId),
        HttpStatus.OK);
  }

  @Override
  public ResponseEntity<CalcomToken> getToken() {
    return new ResponseEntity<>(consultantFacade.getToken(authenticatedUser.getUserId()),
        HttpStatus.OK);
  }
}
