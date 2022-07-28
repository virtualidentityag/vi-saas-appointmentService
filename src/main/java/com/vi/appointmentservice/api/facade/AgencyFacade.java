package com.vi.appointmentservice.api.facade;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vi.appointmentservice.api.exception.httpresponses.CalComApiErrorException;
import com.vi.appointmentservice.api.exception.httpresponses.InternalServerErrorException;
import com.vi.appointmentservice.api.exception.httpresponses.NotFoundException;
import com.vi.appointmentservice.api.model.CreateUpdateCalcomEventTypeDTO;
import com.vi.appointmentservice.repository.UserEventTypeRepository;
import com.vi.appointmentservice.useradminservice.generated.web.model.ConsultantDTO;
import com.vi.appointmentservice.api.exception.httpresponses.BadRequestException;
import com.vi.appointmentservice.api.model.AgencyConsultantSyncRequestDTO;
import com.vi.appointmentservice.api.model.AgencyMasterDataSyncRequestDTO;
import com.vi.appointmentservice.api.model.CalcomEventType;
import com.vi.appointmentservice.api.model.CalcomTeam;
import com.vi.appointmentservice.api.model.MeetingSlug;
import com.vi.appointmentservice.api.model.TeamEventTypeConsultant;
import com.vi.appointmentservice.api.service.calcom.CalComEventTypeService;
import com.vi.appointmentservice.api.service.calcom.team.CalComTeamService;
import com.vi.appointmentservice.api.service.onlineberatung.AdminUserService;
import com.vi.appointmentservice.model.TeamToAgency;
import com.vi.appointmentservice.repository.CalcomUserToConsultantRepository;
import com.vi.appointmentservice.repository.MembershipsRepository;
import com.vi.appointmentservice.repository.TeamRepository;
import com.vi.appointmentservice.repository.TeamToAgencyRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.swing.text.html.Option;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
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
  private final AdminUserService adminUserService;
  @NonNull
  private final UserEventTypeRepository userEventTypeRepository;

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
      if(calcomUserToConsultantRepository.existsByCalComUserId(userId)){
        String onberConsultantId = calcomUserToConsultantRepository.findByCalComUserId(userId).getConsultantId();
        ConsultantDTO consultant = adminUserService.getConsultantById(onberConsultantId);
        TeamEventTypeConsultant teamEventTypeConsultant = new TeamEventTypeConsultant();
        teamEventTypeConsultant.setConsultantId(onberConsultantId);
        teamEventTypeConsultant.setConsultantName(consultant.getUsername());
        availableConsultants.add(teamEventTypeConsultant);
      }
    }
    return availableConsultants;
  }

  public CalcomEventType addEventTypeToAgency(Long agencyId, CreateUpdateCalcomEventTypeDTO eventType){
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
    Long teamid = getTeamIdByAgencyId(agencyId);
    eventTypePayload.setTeamId(Math.toIntExact(teamid));
    if(eventTypePayload.getSlug() == null){
      eventTypePayload.setSlug(UUID.randomUUID().toString());
    }
    eventTypePayload.setHidden(false);
    eventTypePayload.setSchedulingType("ROUND_ROBIN");
    eventTypePayload.setRequiresConfirmation(false);
    JSONObject eventTypePayloadJson;
    try {
      eventTypePayloadJson = new JSONObject(objectMapper.writeValueAsString(eventTypePayload));
    } catch (JsonProcessingException e) {
      throw new InternalServerErrorException("Could not serialize eventTypePayload");
    }
    CalcomEventType createdEventType = calComEventTypeService.createEventType(eventTypePayloadJson);
    // Add all team members to eventType
    // List<Long> userIds = this.getAllConsultantIdsOfAgency(agencyId);
    List<TeamEventTypeConsultant> consultants = eventType.getConsultants();
    userEventTypeRepository.updateUsersOfEventType(Long.valueOf(createdEventType.getId()), consultants);
    // TODO: Do we need to set a schedule for the event-type?
    return createdEventType;
  }

  protected Long getTeamIdByAgencyId(Long agencyId) {
    Optional<TeamToAgency> teamToAgency = teamToAgencyRepository.findByAgencyId(agencyId);
    Long teamid;
    if(teamToAgency.isPresent()){
      teamid = getTeamIdByAgencyId(agencyId);
    } else {
      throw new NotFoundException("No team for agency with the id '" + agencyId +  "'");
    }
    return teamid;
  }

  public List<CalcomEventType> getCalcomEventTypesByAgencyId(Long agencyId) {
    List<CalcomEventType> eventTypes;
    if (teamToAgencyRepository.existsByAgencyId(agencyId)) {
      eventTypes = calComEventTypeService.getAllEventTypesOfTeam(getTeamIdByAgencyId(agencyId));
      return eventTypes;
    } else {
      throw new BadRequestException(
          String.format("No calcom team associated to agency with id: %s", agencyId));
    }
  }

  public MeetingSlug getMeetingSlugByAgencyId(Long agencyId) {
    this.checkIfAgencyTeamExists(agencyId);
    MeetingSlug meetingSlug = new MeetingSlug();
    meetingSlug.setSlug(calComTeamService.getTeamById(
        getTeamIdByAgencyId(agencyId)).getSlug());
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
    Long calComUserId = calcomUserToConsultantRepository.findByConsultantId(consultantId)
        .getCalComUserId();
    List<Long> teamIds = request.getAgencies().stream()
        .map(agencyId -> teamToAgencyRepository.findByAgencyId(agencyId).get().getTeamid())
        .collect(Collectors.toList());
    membershipsRepository.updateMemberShipsOfUser(calComUserId, teamIds);
  }

  public void agencyMasterDataSync(AgencyMasterDataSyncRequestDTO request) {
    Optional<TeamToAgency> teamToAgency = teamToAgencyRepository.findByAgencyId(request.getId());
    if (!teamToAgency.isPresent()) {
      CalcomTeam team = new CalcomTeam();
      team.setName(request.getName());
      team.setHideBranding(true);
      CalcomTeam createdTeam = calComTeamService.createTeam(team);
      Long teamId = createdTeam.getId();
      TeamToAgency entity = new TeamToAgency();
      entity.setTeamid(teamId);
      entity.setAgencyId(request.getId());
      teamToAgencyRepository.save(entity);
    } else {
      CalcomTeam team = new CalcomTeam();
      team.setName(request.getName());
      team.setId(teamToAgency.get().getTeamid());
      calComTeamService.editTeam(team);
    }
  }

  public void deleteAgency(Long agencyId) {
    Optional<TeamToAgency> teamToAgency = teamToAgencyRepository.findByAgencyId(agencyId);
    Long teamId = teamToAgency.get().getTeamid();
    teamRepository.deleteTeam(teamId);
  }
}
