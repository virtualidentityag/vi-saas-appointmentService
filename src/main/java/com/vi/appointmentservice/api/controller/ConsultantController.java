package com.vi.appointmentservice.api.controller;

import com.vi.appointmentservice.api.exception.httpresponses.BadRequestException;
import com.vi.appointmentservice.api.facade.ConsultantFacade;
import com.vi.appointmentservice.api.model.CalcomBooking;
import com.vi.appointmentservice.api.model.CalcomEventType;
import com.vi.appointmentservice.api.model.CalcomUser;
import com.vi.appointmentservice.api.model.ConsultantDTO;
import com.vi.appointmentservice.api.model.MeetingSlug;
import com.vi.appointmentservice.generated.api.controller.ConsultantsApi;
import io.swagger.annotations.Api;
import java.util.List;
import java.util.Objects;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
  public ResponseEntity<CalcomEventType> addEventTypeToConsultant(String consultantId,
      CalcomEventType calcomEventType) {
    return ConsultantsApi.super.addEventTypeToConsultant(consultantId, calcomEventType);
  }

  @Override
  public ResponseEntity<List<CalcomBooking>> getAllBookingsOfConsultant(String consultantId) {
    return new ResponseEntity<>(consultantFacade.getAllBookingsOfConsultantHandler(consultantId),
        HttpStatus.OK);
  }


  @Override
  public ResponseEntity<List<CalcomEventType>> getAllEventTypesOfConsultant(String consultantId) {
    return new ResponseEntity<>(consultantFacade.getAllEventTypesOfConsultantHandler(consultantId),
        HttpStatus.OK);
  }

  @Override
  public ResponseEntity<MeetingSlug> getConsultantMeetingSlug(String consultantId) {
    return new ResponseEntity<>(consultantFacade.getConsultantMeetingSlugHandler(consultantId),
        HttpStatus.OK);
  }


}