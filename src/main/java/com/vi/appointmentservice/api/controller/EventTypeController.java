package com.vi.appointmentservice.api.controller;

import com.vi.appointmentservice.api.facade.EventTypeFacade;
import com.vi.appointmentservice.api.model.CalcomEventType;
import com.vi.appointmentservice.api.model.CreateUpdateCalcomEventTypeDTO;
import com.vi.appointmentservice.api.service.calcom.CalComEventTypeService;
import com.vi.appointmentservice.generated.api.controller.EventTypesApi;
import com.vi.appointmentservice.repository.CalcomUserToConsultantRepository;
import com.vi.appointmentservice.repository.TeamToAgencyRepository;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for event-type API operations.
 */
@RestController
@Api(tags = "eventType")
@Slf4j
public class EventTypeController implements EventTypesApi {


  CalcomUserToConsultantRepository calcomUserToConsultantRepository;
  TeamToAgencyRepository teamToAgencyRepository;
  CalComEventTypeService calComEventTypeService;
  EventTypeFacade eventTypeFacade;

  @Autowired
  public EventTypeController(CalComEventTypeService calComEventTypeService,
      CalcomUserToConsultantRepository calcomUserToConsultantRepository,
      TeamToAgencyRepository teamToAgencyRepository, EventTypeFacade eventTypeFacade) {
    this.calComEventTypeService = calComEventTypeService;
    this.calcomUserToConsultantRepository = calcomUserToConsultantRepository;
    this.teamToAgencyRepository = teamToAgencyRepository;
    this.eventTypeFacade = eventTypeFacade;
  }

  @Override
  public ResponseEntity<Void> deleteEventType(Long eventTypeId) {
    eventTypeFacade.deleteEventType(eventTypeId);
    return new ResponseEntity<>(HttpStatus.OK);
  }


  @Override
  public ResponseEntity<CalcomEventType> getEventTypeById(Long eventTypeId) {
    return new ResponseEntity<>(eventTypeFacade.getEventTypeById(eventTypeId), HttpStatus.OK);
  }

  @Override
  public ResponseEntity<CalcomEventType> updateEventType(Long eventTypeId, CreateUpdateCalcomEventTypeDTO calcomEventType) {
    return new ResponseEntity<>(eventTypeFacade.updateEventType(eventTypeId, calcomEventType), HttpStatus.OK);
  }
}