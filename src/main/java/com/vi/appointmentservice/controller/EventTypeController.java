package com.vi.appointmentservice.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.vi.appointmentservice.api.model.CalcomEventType;
import com.vi.appointmentservice.generated.api.controller.EventTypesApi;
import com.vi.appointmentservice.repository.CalcomUserToConsultantRepository;
import com.vi.appointmentservice.repository.TeamToAgencyRepository;
import com.vi.appointmentservice.service.CalComEventTypeService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
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

    @Autowired
    public EventTypeController(CalComEventTypeService calComEventTypeService, CalcomUserToConsultantRepository calcomUserToConsultantRepository, TeamToAgencyRepository teamToAgencyRepository) {
        this.calComEventTypeService = calComEventTypeService;
        this.calcomUserToConsultantRepository = calcomUserToConsultantRepository;
        this.teamToAgencyRepository = teamToAgencyRepository;
    }

    @Override
    public ResponseEntity<Void> deleteEventType(Long eventTypeId) {
        return new ResponseEntity<>(calComEventTypeService.deleteEventType(eventTypeId));
    }


    @Override
    public ResponseEntity<CalcomEventType> getEventTypeById(Long eventTypeId) {
        return EventTypesApi.super.getEventTypeById(eventTypeId);
    }

    @Override
    public ResponseEntity<CalcomEventType> updateEventType(Long eventTypeId, CalcomEventType calcomEventType) {
        return EventTypesApi.super.updateEventType(eventTypeId, calcomEventType);
    }
}