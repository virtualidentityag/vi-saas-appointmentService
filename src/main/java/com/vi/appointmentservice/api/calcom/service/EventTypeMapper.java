package com.vi.appointmentservice.api.calcom.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vi.appointmentservice.api.calcom.model.CalcomEventType;
import com.vi.appointmentservice.api.model.EventTypeDTO;
import com.vi.appointmentservice.api.model.Location;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EventTypeMapper {

  public EventTypeDTO asEventTypeDTO(CalcomEventType eventType) {
    EventTypeDTO calcomEventType = new EventTypeDTO();
    calcomEventType.setTitle(eventType.getTitle());
    calcomEventType.setId(eventType.getId());
    calcomEventType.setLength(eventType.getLength());
    calcomEventType.setDescription(eventType.getDescription());
    calcomEventType.setConsultants(eventType.getConsultants());
    setLocations(eventType, calcomEventType);
    return calcomEventType;
  }

  private void setLocations(CalcomEventType eventType, EventTypeDTO calcomEventType) {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    try {
      List<Location> locations = objectMapper.readValue(eventType.getLocations(), new TypeReference<List<Location>>(){});
      calcomEventType.setLocations(locations);
    } catch (JsonProcessingException e) {
      log.error("Error while parsing locations of event type {}. Location string: {}", eventType.getId(), eventType.getLocations());
    }
  }
}
