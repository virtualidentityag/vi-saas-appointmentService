package com.vi.appointmentservice.api.facade;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vi.appointmentservice.api.exception.httpresponses.BadRequestException;
import com.vi.appointmentservice.api.exception.httpresponses.CalComApiErrorException;
import com.vi.appointmentservice.api.exception.httpresponses.InternalServerErrorException;
import com.vi.appointmentservice.api.model.CalcomEventType;
import com.vi.appointmentservice.api.model.CalcomTeam;
import com.vi.appointmentservice.api.model.CreateUpdateCalcomEventTypeDTO;
import com.vi.appointmentservice.api.model.TeamEventTypeConsultant;
import com.vi.appointmentservice.api.service.calcom.CalComEventTypeService;
import com.vi.appointmentservice.api.service.calcom.CalComUserService;
import com.vi.appointmentservice.model.CalcomUserToConsultant;
import com.vi.appointmentservice.repository.CalcomUserToConsultantRepository;
import com.vi.appointmentservice.repository.EventTypeRepository;
import com.vi.appointmentservice.repository.TeamToAgencyRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EventTypeFacade {
  @NonNull
  private final CalComEventTypeService calComEventTypeService;
  @NonNull
  private final CalcomUserToConsultantRepository calcomUserToConsultantRepository;
  @NonNull
  private final EventTypeRepository eventTypeRepository;
  @NonNull
  private final TeamToAgencyRepository teamToAgencyRepository;
  @NonNull
  private final AgencyFacade agencyFacade;
  @NonNull
  private final CalComUserService calComUserService;

  @Value("${app.base.url}")
  private String appBaseUrl;

  public List<CalcomEventType> getAgencyEventTypes(Long agencyId) {
    List<CalcomEventType> eventTypes;
    if (teamToAgencyRepository.existsByAgencyId(agencyId)) {
      eventTypes = calComEventTypeService.getAllEventTypesOfTeam(teamToAgencyRepository.findByAgencyId(agencyId).get().getTeamid());
      return eventTypes;
    } else {
      throw new BadRequestException(
          String.format("No calcom team associated to agency with id: %s", agencyId));
    }
  }

  private List<TeamEventTypeConsultant> getConsultantsOfAgencyEventType(Long eventTypeId){
    List<TeamEventTypeConsultant> consultants = new ArrayList<>();
    // Get consultant Ids
    for(Long userId : eventTypeRepository.getUserIdsOfEventTypeMembers(eventTypeId)){
      // TODO: refactor int Optional<CalcomUserToConsultant> to avoid double call
      if (calcomUserToConsultantRepository.existsByCalComUserId(userId)){
        TeamEventTypeConsultant teamEventTypeConsultant = new TeamEventTypeConsultant();
        teamEventTypeConsultant.setConsultantName(calComUserService.getUserById(userId).getName());
        teamEventTypeConsultant.setConsultantId(calcomUserToConsultantRepository.findByCalComUserId(userId).getConsultantId());
        consultants.add(teamEventTypeConsultant);
      }
    }
    return consultants;
  }

  public CalcomEventType getAgencyEventTypeById(Long eventTypeId){
    CalcomEventType eventType = calComEventTypeService.getEventTypeById(eventTypeId);
    eventType.setConsultants(this.getConsultantsOfAgencyEventType(Long.valueOf(eventType.getId())));
    return eventType;
  }

  public CalcomEventType addAgencyEventType(Long agencyId, CreateUpdateCalcomEventTypeDTO eventType){
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

    // Create event-type
    CalcomEventType eventTypePayload;
    try {
      eventTypePayload = objectMapper.readValue(new JSONObject(eventType).toString(), CalcomEventType.class);
    } catch (JsonProcessingException e) {
      throw new CalComApiErrorException("Could not deserialize CreateUpdateCalcomEventTypeDTO to CalcomEventType while adding eventType to team");
    }
    Long teamid = agencyFacade.getTeamIdByAgencyId(agencyId);
    eventTypePayload.setTeamId(Math.toIntExact(teamid));
    if(eventTypePayload.getSlug() == null){
      eventTypePayload.setSlug(UUID.randomUUID().toString());
    }
    eventTypePayload.setHidden(false);
    eventTypePayload.setSchedulingType("ROUND_ROBIN");
    eventTypePayload.setRequiresConfirmation(false);
    eventTypePayload.setEventName(eventType.getTitle());
    eventTypePayload.setRequiresConfirmation(false);
    eventTypePayload.setDisableGuests(true);
    eventTypePayload.setHideCalendarNotes(true);
    eventTypePayload.setSuccessRedirectUrl(
        appBaseUrl + "/sessions/user/view/");

    JSONObject eventTypePayloadJson;
    try {
      eventTypePayloadJson = new JSONObject(objectMapper.writeValueAsString(eventTypePayload));
    } catch (JsonProcessingException e) {
      throw new InternalServerErrorException("Could not serialize eventTypePayload");
    }
    CalcomEventType createdEventType = calComEventTypeService.createEventType(eventTypePayloadJson);

    // Add consultants to eventType
    List<TeamEventTypeConsultant> consultants = eventType.getConsultants();
    eventTypeRepository.updateUsersOfEventType(Long.valueOf(createdEventType.getId()), consultants);
    return createdEventType;
  }

  public CalcomEventType updateAgencyEventType(Long eventTypeId, CreateUpdateCalcomEventTypeDTO eventType){
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

    // Add consultants to eventType
    List<TeamEventTypeConsultant> consultants = eventType.getConsultants();
    eventTypeRepository.updateUsersOfEventType(Long.valueOf(updatedEventType.getId()), consultants);
    return updatedEventType;
  }

  public void deleteAgencyEventType(Long eventTypeId){
    calComEventTypeService.deleteEventType(eventTypeId);
    eventTypeRepository.removeTeamEventTypeMembershipsForEventType(eventTypeId);
  }

  CalcomEventType getDefaultCalcomInitialMeetingEventType(CalcomTeam team) {
    CalcomEventType eventType = new CalcomEventType();
    eventType.setTeamId(Math.toIntExact(team.getId()));
    eventType.setTitle("Erstberatung " + team.getName());
    eventType.setSlug(UUID.randomUUID().toString());
    eventType.setLength(60);
    eventType.setHidden(false);
    eventType.setEventName("Erstberatung {ATTENDEE} mit {HOST}");
    eventType.setRequiresConfirmation(false);
    eventType.setDisableGuests(true);
    eventType.setHideCalendarNotes(true);
    eventType.setMinimumBookingNotice(120);
    eventType.setBeforeEventBuffer(0);
    eventType.setAfterEventBuffer(0);
    eventType.setSuccessRedirectUrl(
        appBaseUrl + "/sessions/user/view/");
    eventType.setDescription("");
    eventType.setSchedulingType("ROUND_ROBIN");
    return eventType;
  }

}
