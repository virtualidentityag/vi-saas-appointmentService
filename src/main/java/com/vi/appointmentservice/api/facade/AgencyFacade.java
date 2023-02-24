package com.vi.appointmentservice.api.facade;

import com.vi.appointmentservice.api.calcom.model.EventType;
import com.vi.appointmentservice.api.calcom.service.CalComUserService;
import com.vi.appointmentservice.api.calcom.service.CalcomEventTypeService;
import com.vi.appointmentservice.api.calcom.service.CalComTeamService;
import com.vi.appointmentservice.api.exception.httpresponses.BadRequestException;
import com.vi.appointmentservice.api.model.AgencyMasterDataSyncRequestDTO;
import com.vi.appointmentservice.api.model.CreateUpdateCalcomEventTypeDTO;
import com.vi.appointmentservice.api.model.MeetingSlug;
import com.vi.appointmentservice.api.model.TeamEventTypeConsultant;
import com.vi.appointmentservice.model.CalcomUserToConsultant;
import com.vi.appointmentservice.model.TeamToAgency;
import com.vi.appointmentservice.repository.UserToConsultantRepository;
import com.vi.appointmentservice.repository.TeamToAgencyRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/*
 * Facade to encapsulate agency operations
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AgencyFacade {

  @NonNull
  private final TeamToAgencyRepository teamToAgencyRepository;

  @NonNull
  private final UserToConsultantRepository user2ConsultantRepo;

  @NonNull
  private final CalComUserService calComUserService;

  @NonNull
  private final CalComTeamService calComTeamService;

  @NonNull
  private final CalcomEventTypeService calcomEventTypeService;

  private static final String DEFAULT_EVENT_DESCRIPTION =
      "Bitte wählen Sie Ihre gewünschte Terminart. Wir bemühen uns, Ihren Wunsch zu erfüllen. "
          + "Die Berater:innen werden Sie ggf per Chat auf unserer Plattform informieren. "
          + "Loggen Sie sich also vor einem Termin auf jeden Fall ein!";

  public void agencyMasterDataSync(AgencyMasterDataSyncRequestDTO request) {
    if (appointmentTeamExist(request.getId())) {
      updateAppointmentTeam(request.getId(), request.getName());
    } else {
      createTeam(request.getId(), request.getName());
    }
  }

  private boolean appointmentTeamExist(Long id) {
    return teamToAgencyRepository.existsByAgencyId(id);
  }

  private void updateAppointmentTeam(Long agencyId, String agencyName) {
    var teamId = toTeamId(agencyId);
    calComTeamService.updateTeam(teamId, agencyName);
  }

  private void createTeam(Long agencyId, String agencyName) {
    AppointmentType defaultAppointmentType = buildDefaultAppointmentType();
    var calTeam = calComTeamService.createTeam(agencyName);
    defaultAppointmentType.setTitle("Erstbefragung " + calTeam.getName());
    EventType eventType = calcomEventTypeService
        .createEventType(calTeam.getId(), defaultAppointmentType);
    calcomEventTypeService.markAsDefaultEventType(eventType);
    TeamToAgency appointmentTeam = new TeamToAgency();
    appointmentTeam.setTeamid(calTeam.getId());
    appointmentTeam.setAgencyId(agencyId);
    teamToAgencyRepository.save(appointmentTeam);
  }

  private AppointmentType buildDefaultAppointmentType() {
    AppointmentType appointmentType = new AppointmentType();
    appointmentType.setAfterEventBuffer(10);
    appointmentType.setBeforeEventBuffer(0);
    appointmentType.setDescription(DEFAULT_EVENT_DESCRIPTION);
    appointmentType.setLength(50);
    appointmentType.setMinimumBookingNotice(240);
    appointmentType.setSlotInterval(15);
    return appointmentType;
  }

  private List<Long> getAppointmentTeams4Agencies(List<Long> agencyIds) {
    List<Long> appointmentTeamsIds = new ArrayList<>();
    agencyIds.forEach(agencyId -> {
      var team2Agency = teamToAgencyRepository.findByAgencyId(agencyId);
      if (team2Agency.isPresent()) {
        appointmentTeamsIds.add(team2Agency.get().getTeamid());
      }
    });
    return appointmentTeamsIds;
  }


  public void assignConsultant2AppointmentTeams(String consultantId, List<Long> agencyIds) {
    Optional<CalcomUserToConsultant> appointmentUser = user2ConsultantRepo
        .findByConsultantId(consultantId);
    var appointmentTeamsIds = getAppointmentTeams4Agencies(agencyIds);
    var calcomUserId = appointmentUser.get().getCalComUserId();
    calcomEventTypeService.cleanUserMemberships(calcomUserId, appointmentTeamsIds);
    appointmentTeamsIds.forEach(teamId -> {
      calcomEventTypeService.addUser2Team(calcomUserId, teamId);
    });
  }

  public List<TeamEventTypeConsultant> getAllConsultantsOfAgency(Long agencyId) {
    var teamId = toTeamId(agencyId);
    List<Long> teamMembers = calComTeamService.getTeamMembers(teamId);
    List<TeamEventTypeConsultant> availableConsultants = new ArrayList<>();
    teamMembers.forEach(teamMember -> {
      TeamEventTypeConsultant teamEventTypeConsultant = new TeamEventTypeConsultant();
      teamEventTypeConsultant
          .setConsultantName(calComUserService.getUserById(teamMember).getName());
      var consultantId = user2ConsultantRepo.findByCalComUserId(teamMember).get()
          .getConsultantId();
      teamEventTypeConsultant.setConsultantId(consultantId);
      availableConsultants.add(teamEventTypeConsultant);
    });

    return availableConsultants;
  }

  public MeetingSlug getMeetingSlugByAgencyId(Long agencyId) {
    MeetingSlug meetingSlug = new MeetingSlug();
    Long teamId = toTeamId(agencyId);
    meetingSlug.setSlug(calComTeamService.getTeamById(teamId).getSlug());
    return meetingSlug;
  }

  public void deleteAgency(Long agencyId) {
    var teamAgency = toTeamAgency(agencyId);
    calComTeamService.deleteTeam(teamAgency.getTeamid());
    teamToAgencyRepository.delete(teamAgency);
  }

  public List<EventType> getAgencyEventTypes(Long agencyId) {
    var teamId = toTeamId(agencyId);
    var eventTypes = calcomEventTypeService.getAllEventTypesOfTeam(teamId);
    eventTypes.forEach(eventType -> {
      attachConsultantsInformationToEventType(eventType);
      attachFullSlugToEventType(eventType);
    });
    return eventTypes;
  }

  private Long toTeamId(Long agencyId) {
    return toTeamAgency(agencyId).getTeamid();
  }

  private TeamToAgency toTeamAgency(Long agencyId) {
    var team2Agency = teamToAgencyRepository.findByAgencyId(agencyId);
    if (team2Agency.isEmpty()) {
      throw new BadRequestException("No team found for given agency");
    }
    return team2Agency.get();
  }

  private EventType attachConsultantsInformationToEventType(EventType eventType) {
    List<TeamEventTypeConsultant> consultants = new ArrayList<>();
    eventType.getMemberIds().forEach(member -> {
      Optional<CalcomUserToConsultant> calomUserToConsultant = user2ConsultantRepo
          .findByCalComUserId(member);
      TeamEventTypeConsultant teamEventTypeConsultant = new TeamEventTypeConsultant();
      teamEventTypeConsultant.setConsultantName(calComUserService.getUserById(member).getName());
      teamEventTypeConsultant.setConsultantId(calomUserToConsultant.get().getConsultantId());
      consultants.add(teamEventTypeConsultant);
    });
    eventType.setConsultants(consultants);
    return eventType;
  }

  public EventType getAgencyEventTypeById(Long eventTypeId) {
    EventType eventType = calcomEventTypeService.getEventTypeById(eventTypeId);
    attachConsultantsInformationToEventType(eventType);
    attachFullSlugToEventType(eventType);
    return eventType;
  }

  public EventType attachFullSlugToEventType(EventType calcomEventType) {
    String eventTypeSlug = calcomEventType.getSlug();
    String teamSlug = calComTeamService.getTeamById(calcomEventType.getTeamId())
        .getSlug();
    calcomEventType.setSlug(teamSlug + "/" + eventTypeSlug);
    return calcomEventType;
  }

  public EventType createAgencyEventType(Long agencyId,
      CreateUpdateCalcomEventTypeDTO eventType) {
    Long teamid = toTeamId(agencyId);
    AppointmentType appointmentType = buildDefaultAppointmentType();
    appointmentType.setTitle(eventType.getTitle());
    appointmentType.setLength(eventType.getLength());
    appointmentType.setDescription(eventType.getDescription());
    EventType eventType1 = calcomEventTypeService.createEventType(teamid, appointmentType);
    eventType.getConsultants().forEach(consultant -> {
      List<Long> agencies = new ArrayList<>();
      agencies.add(agencyId);
      assignConsultant2AppointmentTeams(consultant.getConsultantId(), agencies);
    });
    attachConsultantsInformationToEventType(eventType1);
    attachFullSlugToEventType(eventType1);
    return eventType1;
  }

  public EventType updateAgencyEventType(Long eventTypeId,
      CreateUpdateCalcomEventTypeDTO eventType) {

    EventType eventTypeDB = calcomEventTypeService.getEventTypeById(eventTypeId);
    eventTypeDB.setTitle(eventType.getTitle());
    eventTypeDB.setLength(eventType.getLength());
    eventTypeDB.setDescription(eventType.getDescription());

    List<Long> eventMembers = new ArrayList<>();
    eventType.getConsultants().forEach(consultant -> {
      Optional<CalcomUserToConsultant> user = user2ConsultantRepo
          .findByConsultantId(consultant.getConsultantId());
      if (user.isPresent()) {
        eventMembers.add(user.get().getCalComUserId());
      }
    });
    eventTypeDB.setMemberIds(eventMembers);
    return calcomEventTypeService.updateEventType(eventTypeDB);
  }

  public void deleteAgencyEventType(Long eventTypeId) {
//    calComEventTypeService.deleteEventType(eventTypeId);
//    eventTypeRepository.removeTeamEventTypeMembershipsForEventType(eventTypeId);
  }
}
