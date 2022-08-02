package com.vi.appointmentservice.api.facade;

import com.vi.appointmentservice.api.exception.httpresponses.BadRequestException;
import com.vi.appointmentservice.api.model.AgencyConsultantSyncRequestDTO;
import com.vi.appointmentservice.api.model.AgencyMasterDataSyncRequestDTO;
import com.vi.appointmentservice.api.model.CalcomEventType;
import com.vi.appointmentservice.api.model.CalcomTeam;
import com.vi.appointmentservice.api.model.MeetingSlug;
import com.vi.appointmentservice.api.service.calcom.CalComEventTypeService;
import com.vi.appointmentservice.api.service.calcom.team.CalComTeamService;
import com.vi.appointmentservice.model.TeamToAgency;
import com.vi.appointmentservice.repository.CalcomUserToConsultantRepository;
import com.vi.appointmentservice.repository.MembershipsRepository;
import com.vi.appointmentservice.repository.TeamRepository;
import com.vi.appointmentservice.repository.TeamToAgencyRepository;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
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


  public List<CalcomEventType> getCalcomEventTypesByAgencyId(Long agencyId) {
    List<CalcomEventType> eventTypes;
    if (teamToAgencyRepository.existsByAgencyId(agencyId)) {
      eventTypes = calComEventTypeService.getAllEventTypesOfTeam(teamToAgencyRepository.findByAgencyId(agencyId).get().getTeamid());
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
    Long calComUserId = calcomUserToConsultantRepository.findByConsultantId(consultantId)
        .getCalComUserId();
    List<Long> teamIds = request.getAgencies().stream()
        .filter(teamToAgencyRepository::existsByAgencyId)
        .map(agencyId -> teamToAgencyRepository.findByAgencyId(agencyId).get().getTeamid())
        .collect(Collectors.toList());
    membershipsRepository.updateMemberShipsOfUser(calComUserId, teamIds);
  }

  public void agencyMasterDataSync(AgencyMasterDataSyncRequestDTO request) {
    Optional<TeamToAgency> teamToAgency = teamToAgencyRepository.findByAgencyId(request.getId());
    if (teamToAgency.isEmpty()) {
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
    if(teamToAgency.isPresent()){
      Long teamId = teamToAgency.get().getTeamid();
      membershipsRepository.deleteTeamMemeberships(teamId);
      teamRepository.deleteTeam(teamId);
      // TODO: Delete event-types?
      // TODO: Cancel Bookings?
    }
  }
}
