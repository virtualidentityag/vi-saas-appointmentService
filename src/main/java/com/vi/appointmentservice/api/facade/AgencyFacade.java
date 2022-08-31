package com.vi.appointmentservice.api.facade;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vi.appointmentservice.api.exception.httpresponses.BadRequestException;
import com.vi.appointmentservice.api.exception.httpresponses.CalComApiErrorException;
import com.vi.appointmentservice.api.exception.httpresponses.InternalServerErrorException;
import com.vi.appointmentservice.api.exception.httpresponses.NotFoundException;
import com.vi.appointmentservice.api.model.AgencyConsultantSyncRequestDTO;
import com.vi.appointmentservice.api.model.AgencyMasterDataSyncRequestDTO;
import com.vi.appointmentservice.api.model.CalcomEventType;
import com.vi.appointmentservice.api.model.CalcomEventTypeDTO;
import com.vi.appointmentservice.api.model.CalcomTeam;
import com.vi.appointmentservice.api.model.CreateUpdateCalcomEventTypeDTO;
import com.vi.appointmentservice.api.model.MeetingSlug;
import com.vi.appointmentservice.api.model.TeamEventTypeConsultant;
import com.vi.appointmentservice.api.service.calcom.CalComEventTypeService;
import com.vi.appointmentservice.api.service.calcom.CalComUserService;
import com.vi.appointmentservice.api.service.calcom.team.CalComTeamService;
import com.vi.appointmentservice.model.CalcomUserToConsultant;
import com.vi.appointmentservice.model.TeamToAgency;
import com.vi.appointmentservice.repository.CalcomUserToConsultantRepository;
import com.vi.appointmentservice.repository.EventTypeRepository;
import com.vi.appointmentservice.repository.MembershipsRepository;
import com.vi.appointmentservice.repository.TeamRepository;
import com.vi.appointmentservice.repository.TeamToAgencyRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/*
 * Facade to encapsulate agency operations
 */
@Component
@RequiredArgsConstructor
public class AgencyFacade {

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
  private final EventTypeRepository eventTypeRepository;
  @NonNull
  private final CalComUserService calComUserService;
  @NonNull
  private final ConsultantFacade consultantFacade;


  @Value("${app.base.url}")
  private String appBaseUrl;

  public List<Long> getAllConsultantIdsOfAgency(Long agencyId){
    Optional<TeamToAgency> teamToAgency = teamToAgencyRepository.findByAgencyId(agencyId);
    if (teamToAgency.isPresent()) {
      Long teamId = teamToAgency.get().getTeamid();
      return membershipsRepository.getUsersOfTeam(teamId);
    } else {
      throw new NotFoundException("No teams associate to agency with id '" +agencyId+ "' found");
    }
  }


  public List<TeamEventTypeConsultant> getAllConsultantsOfAgency(Long agencyId){
    List<TeamEventTypeConsultant> availableConsultants = new ArrayList<>();
    for(Long userId : this.getAllConsultantIdsOfAgency(agencyId)){
      Optional<CalcomUserToConsultant> calcomUserToConsultant = calcomUserToConsultantRepository.findByCalComUserId(userId);
      if (calcomUserToConsultant.isPresent()){
        TeamEventTypeConsultant teamEventTypeConsultant = new TeamEventTypeConsultant();
        teamEventTypeConsultant.setConsultantName(calComUserService.getUserById(userId).getName());
        teamEventTypeConsultant.setConsultantId(calcomUserToConsultant.get().getConsultantId());
        availableConsultants.add(teamEventTypeConsultant);
      }
    }
    return availableConsultants;
  }

  public MeetingSlug getMeetingSlugByAgencyId(Long agencyId) {
    this.checkIfAgencyTeamExists(agencyId);
    MeetingSlug meetingSlug = new MeetingSlug();
    meetingSlug.setSlug(calComTeamService.getTeamById(
        teamToAgencyRepository.findByAgencyId(agencyId).get().getTeamid()).getSlug());
    return meetingSlug;
  }



  private void checkIfAgencyTeamExists(Long agencyId) {
    if (!teamToAgencyRepository.existsByAgencyId(agencyId)) {
      throw new BadRequestException(
          String.format("No calcom team associated to agency with id: %s", agencyId));
    }
  }

  public void agencyConsultantsSync(AgencyConsultantSyncRequestDTO request) {
    String consultantId = request.getConsultantId();
    Optional<CalcomUserToConsultant> calcomUserToConsultant = calcomUserToConsultantRepository.findByConsultantId(consultantId);
        //.getCalComUserId();
    if(calcomUserToConsultant.isPresent()) {
      consultantFacade.updateUserDefaultEntities(calComUserService.getUserById(calcomUserToConsultant.get().getCalComUserId()));
      List<Long> teamIds = request.getAgencies().stream()
          .filter(teamToAgencyRepository::existsByAgencyId)
          .map(agencyId -> teamToAgencyRepository.findByAgencyId(agencyId).get().getTeamid())
          .collect(Collectors.toList());
      membershipsRepository.updateMemberShipsOfUser(calcomUserToConsultant.get().getCalComUserId(), teamIds);
      // Reset user teamEventTypeMemberships
      eventTypeRepository.removeTeamEventTypeMembershipsForUser(calcomUserToConsultant.get().getCalComUserId(), teamIds);
      // Add consultant to team eventTypes
      for (Long teamId : teamIds) {
        CalcomEventTypeDTO eventType = calComEventTypeService.getDefaultEventTypeOfTeam(teamId);
        eventTypeRepository.addUserEventTypeRelation(Long.valueOf(eventType.getId()),
            calcomUserToConsultant.get().getCalComUserId());
      }
    }
  }

  public void agencyMasterDataSync(AgencyMasterDataSyncRequestDTO request) {
    Optional<TeamToAgency> teamToAgency = teamToAgencyRepository.findByAgencyId(request.getId());
    CalcomTeam createdOrUpdateTeam;
    if (teamToAgency.isEmpty()) {
      CalcomTeam team = new CalcomTeam();
      team.setName(request.getName());
      team.setHideBranding(true);
      createdOrUpdateTeam = calComTeamService.createTeam(team);
      Long teamId = createdOrUpdateTeam.getId();
      TeamToAgency entity = new TeamToAgency();
      entity.setTeamid(teamId);
      entity.setAgencyId(request.getId());
      teamToAgencyRepository.save(entity);
    } else {
      CalcomTeam team = new CalcomTeam();
      team.setName(request.getName());
      team.setId(teamToAgency.get().getTeamid());
      createdOrUpdateTeam = calComTeamService.editTeam(team);
    }
    // Create default team eventType if none exists
    if(createdOrUpdateTeam != null && calComEventTypeService.getAllEventTypesOfTeam(createdOrUpdateTeam.getId()).isEmpty()){
      ObjectMapper objectMapper = new ObjectMapper();
      // Ignore null values
      objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
      JSONObject eventTypePayloadJson = null;
      try {
        eventTypePayloadJson = new JSONObject(objectMapper.writeValueAsString(getDefaultCalcomInitialMeetingEventType(createdOrUpdateTeam)));
      } catch (JsonProcessingException e) {
        throw new InternalServerErrorException("Could not serialize createCalcomUser payload");
      }
      calComEventTypeService.createEventType(eventTypePayloadJson);
    }
  }

  private CalcomEventTypeDTO getDefaultCalcomInitialMeetingEventType(CalcomTeam team) {
    CalcomEventTypeDTO eventType = new CalcomEventTypeDTO();
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
    eventType.setMetadata("{defaultEventType: 'true'}");
    eventType.setDescription("");
    eventType.setSchedulingType("ROUND_ROBIN");
    return eventType;
  }

  public void deleteAgency(Long agencyId) {
    Optional<TeamToAgency> teamToAgency = teamToAgencyRepository.findByAgencyId(agencyId);
    if(teamToAgency.isPresent()){
      Long teamId = teamToAgency.get().getTeamid();
      membershipsRepository.deleteTeamMemeberships(teamId);
      teamRepository.deleteTeam(teamId);
      // TODO: Delete event-types?
      // TODO: Cancel Bookings?
    }
  }

  public List<CalcomEventTypeDTO> getAgencyEventTypes(Long agencyId) {
    List<CalcomEventTypeDTO> eventTypes;
    if (teamToAgencyRepository.existsByAgencyId(agencyId)) {
      eventTypes = calComEventTypeService.getAllEventTypesOfTeam(teamToAgencyRepository.findByAgencyId(agencyId).get().getTeamid());
      for (CalcomEventTypeDTO eventTypeDTO : eventTypes){
        attachConsultantsInformationToEventType(eventTypeDTO);
        attachFullSlugToEventType(eventTypeDTO);
      }
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
      Optional<CalcomUserToConsultant> calomUserToConsultant = calcomUserToConsultantRepository.findByCalComUserId(userId);
      if (calomUserToConsultant.isPresent()){
        TeamEventTypeConsultant teamEventTypeConsultant = new TeamEventTypeConsultant();
        teamEventTypeConsultant.setConsultantName(calComUserService.getUserById(userId).getName());
        teamEventTypeConsultant.setConsultantId(calomUserToConsultant.get().getConsultantId());
        consultants.add(teamEventTypeConsultant);
      }
    }
    return consultants;
  }

  public CalcomEventTypeDTO getAgencyEventTypeById(Long eventTypeId){
    CalcomEventTypeDTO eventType = calComEventTypeService.getEventTypeById(eventTypeId);
    attachConsultantsInformationToEventType(eventType);
    attachFullSlugToEventType(eventType);
    return eventType;
  }
  public CalcomEventTypeDTO attachConsultantsInformationToEventType(CalcomEventTypeDTO eventType){
    eventType.setConsultants(this.getConsultantsOfAgencyEventType(Long.valueOf(eventType.getId())));
    return eventType;
  }

  public CalcomEventTypeDTO attachFullSlugToEventType(CalcomEventTypeDTO calcomEventType){
    String eventTypeSlug = calcomEventType.getSlug();
    String teamSlug = calComTeamService.getTeamById(Long.valueOf(calcomEventType.getTeamId())).getSlug();
    calcomEventType.setSlug(teamSlug + "/" + eventTypeSlug);
    return calcomEventType;
  }

  public CalcomEventTypeDTO addAgencyEventType(Long agencyId, CreateUpdateCalcomEventTypeDTO eventType){
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
    Optional<TeamToAgency> teamToAgency = teamToAgencyRepository.findByAgencyId(agencyId);
    Long teamid;
    if(teamToAgency.isPresent()){
      teamid = teamToAgency.get().getTeamid();
    } else {
      throw new NotFoundException("No team for agency with the id '" + agencyId +  "'");
    }
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
    // eventTypePayload.setSuccessRedirectUrl( appBaseUrl + "/sessions/user/view/");
    JSONObject eventTypePayloadJson;
    try {
      eventTypePayloadJson = new JSONObject(objectMapper.writeValueAsString(eventTypePayload));
    } catch (JsonProcessingException e) {
      throw new InternalServerErrorException("Could not serialize eventTypePayload");
    }
    CalcomEventTypeDTO createdEventType = calComEventTypeService.createEventType(eventTypePayloadJson);

    // Add consultants to eventType
    List<TeamEventTypeConsultant> consultants = eventType.getConsultants();
    eventTypeRepository.updateUsersOfEventType(Long.valueOf(createdEventType.getId()), consultants);
    attachConsultantsInformationToEventType(createdEventType);
    attachFullSlugToEventType(createdEventType);
    return createdEventType;
  }

  public CalcomEventTypeDTO updateAgencyEventType(Long eventTypeId, CreateUpdateCalcomEventTypeDTO eventType){
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
    JSONObject eventTypePayloadJson;
    try {
      eventTypePayloadJson = new JSONObject(objectMapper.writeValueAsString(eventTypePayload));
    } catch (JsonProcessingException e) {
      throw new InternalServerErrorException("Could not serialize eventTypePayload");
    }
    eventTypePayloadJson.remove("description");
    eventTypeRepository.updateEventTypeDescription(eventTypeId, eventType.getDescription());
    CalcomEventTypeDTO updatedEventType = calComEventTypeService.editEventType(eventTypeId, eventTypePayloadJson);
    // Add consultants to eventType
    List<TeamEventTypeConsultant> consultants = eventType.getConsultants();
    eventTypeRepository.updateUsersOfEventType(Long.valueOf(updatedEventType.getId()), consultants);
    attachConsultantsInformationToEventType(updatedEventType);
    attachFullSlugToEventType(updatedEventType);
    return updatedEventType;
  }

  public void deleteAgencyEventType(Long eventTypeId){
    calComEventTypeService.deleteEventType(eventTypeId);
    eventTypeRepository.removeTeamEventTypeMembershipsForEventType(eventTypeId);
  }
}
