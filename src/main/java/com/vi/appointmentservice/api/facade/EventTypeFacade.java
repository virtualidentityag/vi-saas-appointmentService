package com.vi.appointmentservice.api.facade;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vi.appointmentservice.api.exception.httpresponses.InternalServerErrorException;
import com.vi.appointmentservice.api.exception.httpresponses.NotFoundException;
import com.vi.appointmentservice.api.model.CalcomEventType;
import com.vi.appointmentservice.api.model.CreateUpdateCalcomEventTypeDTO;
import com.vi.appointmentservice.api.model.TeamEventTypeConsultant;
import com.vi.appointmentservice.api.service.calcom.CalComEventTypeService;
import com.vi.appointmentservice.api.service.calcom.team.CalComTeamService;
import com.vi.appointmentservice.api.service.onlineberatung.AdminUserService;
import com.vi.appointmentservice.model.TeamToAgency;
import com.vi.appointmentservice.repository.CalcomUserToConsultantRepository;
import com.vi.appointmentservice.repository.MembershipsRepository;
import com.vi.appointmentservice.repository.TeamRepository;
import com.vi.appointmentservice.repository.TeamToAgencyRepository;
import com.vi.appointmentservice.repository.UserEventTypeRepository;
import java.util.List;
import java.util.Optional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.NotImplementedException;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EventTypeFacade {

  @NonNull
  private final CalComTeamService calComTeamService;
  @NonNull
  private final CalComEventTypeService calComEventTypeService;
  @NonNull
  private final TeamToAgencyRepository teamToAgencyRepository;
  @NonNull
  private final CalcomUserToConsultantRepository calcomUserToConsultantRepository;
  @NonNull
  private final MembershipsRepository membershipsRepository;
  @NonNull
  private final TeamRepository teamRepository;
  @NonNull
  private final AdminUserService adminUserService;
  @NonNull
  private final UserEventTypeRepository userEventTypeRepository;
  @NonNull
  private final AgencyFacade agencyFacade;

  public CalcomEventType getEventTypeById(Long eventTypeId){
    return calComEventTypeService.getEventTypeById(eventTypeId);
  }

  public CalcomEventType updateEventType(Long eventTypeId, CreateUpdateCalcomEventTypeDTO eventType){
    CalcomEventType existingEventType = this.getEventTypeById(eventTypeId);
    if(existingEventType.getTeamId() != null){
      return updateEventTypeOfAgency(eventTypeId, eventType);
    } else if (existingEventType.getUserId() != null) {
      throw new NotImplementedException("EventType updateing for consultants not yet implemented!");
    } else {
      throw new InternalServerErrorException("Trying to update invalid eventType with both userId and teamId set!");
    }
  }

  public CalcomEventType updateEventTypeOfAgency(Long eventTypeId, CreateUpdateCalcomEventTypeDTO eventType){
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    // Create event-type
    JSONObject eventTypePayloadJson;
    try {
      eventTypePayloadJson = new JSONObject(objectMapper.writeValueAsString(eventType));
    } catch (JsonProcessingException e) {
      throw new InternalServerErrorException("Could not serialize eventTypePayload");
    }
    CalcomEventType updatedEventType = calComEventTypeService.editEventType(eventTypeId, eventTypePayloadJson);
    // Add all team members to eventType
    List<TeamEventTypeConsultant> consultants = eventType.getConsultants();
    userEventTypeRepository.updateUsersOfEventType(Long.valueOf(updatedEventType.getId()), consultants);
    // TODO: Do we need to set a schedule for the event-type?
    return updatedEventType;
  }

  public void deleteEventType(Long eventTypeId){
    calComEventTypeService.deleteEventType(eventTypeId);
    // TODO: Delete UserEventType association
  }

  /*
  public CalcomEventType updateEventTypeOfConsultant(Long consultantId, Long eventTypeId, CreateUpdateCalcomEventTypeDTO eventType){
    // TODO
  }
   */

}
