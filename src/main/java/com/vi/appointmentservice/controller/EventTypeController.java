package com.vi.appointmentservice.controller;

import com.vi.appointmentservice.api.model.EventType;
import com.vi.appointmentservice.generated.api.controller.EventTypeApi;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for event-type API operations.
 */
@RestController
@RequiredArgsConstructor
@Api(tags = "eventType")
@Slf4j
public class EventTypeController implements EventTypeApi {

    @Override
    public ResponseEntity<Void> deleteEventType(Long eventTypeId) {
        return EventTypeApi.super.deleteEventType(eventTypeId);
    }

    @Override
    public ResponseEntity<EventType> getEventTypeById(Long eventTypeId) {
        return EventTypeApi.super.getEventTypeById(eventTypeId);
    }

    @Override
    public ResponseEntity<Void> updateEventType(Long eventTypeId) {
        return EventTypeApi.super.updateEventType(eventTypeId);
    }
}
