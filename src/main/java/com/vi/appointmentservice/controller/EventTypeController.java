package com.vi.appointmentservice.controller;

import com.vi.appointmentservice.api.model.CalcomEventType;
import com.vi.appointmentservice.generated.api.controller.EventTypesApi;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for event-type API operations.
 */
@RestController
@Api(tags = "eventType")
@Slf4j
public class EventTypeController implements EventTypesApi {
    @Override
    public ResponseEntity<Void> deleteEventType(Long eventTypeId) {

        return EventTypesApi.super.deleteEventType(eventTypeId);
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