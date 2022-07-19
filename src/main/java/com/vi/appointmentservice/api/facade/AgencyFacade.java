package com.vi.appointmentservice.api.facade;

import com.vi.appointmentservice.api.exception.httpresponses.NotFoundException;
import com.vi.appointmentservice.api.model.CalcomEventType;
import com.vi.appointmentservice.api.model.MeetingSlug;
import com.vi.appointmentservice.api.service.calcom.CalComEventTypeService;
import com.vi.appointmentservice.api.service.calcom.CalComTeamService;
import com.vi.appointmentservice.repository.TeamToAgencyRepository;
import java.util.List;
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


  public List<CalcomEventType> getCalcomEventTypesByAgencyId(Long agencyId) {
    List<CalcomEventType> eventTypes;
    // TODO: remove "true" once team are associated to agencies and comment in correct line
    if (true || teamToAgencyRepository.existsByAgencyId(Long.valueOf(agencyId))) {
      //eventTypes = calComEventTypeService.getAllEventTypesOfTeam(teamToAgencyRepository.findByAgencyId(agencyId).get(0).getTeamid());
      eventTypes = calComEventTypeService.getAllEventTypesOfTeam(0L);
      return eventTypes;
    } else {
      throw new NotFoundException(
          String.format("No calcom team associated to agency with id: %s", agencyId));
    }
  }

  // TODO: remove method once agencies are associated
  public MeetingSlug getMockMeetingSlugByAgencyId(Long agencyId) {
    MeetingSlug meetingSlug = new MeetingSlug();
    switch (agencyId.intValue()) {
      case 1:
        meetingSlug.setSlug("team-munich");
        break;
      case 2:
        meetingSlug.setSlug("team-hamburg");
        break;
      default:
        throw new NotFoundException(
            String.format("Mock route not configured for agencyId: %s", agencyId));
    }
    return meetingSlug;
  }

  public MeetingSlug getMeetingSlugByAgencyId(Long agencyId) {
    // TODO: remove "mock" method once agencies are associated
    // TODO: add verification, sanitization and general cleanliness
    if (teamToAgencyRepository.existsByAgencyId(agencyId)) {
      MeetingSlug meetingSlug = new MeetingSlug();
      meetingSlug.setSlug(calComTeamService.getTeamById(
          teamToAgencyRepository.findByAgencyId(agencyId).get(0).getTeamid()).getSlug());
      return meetingSlug;
    } else {
      throw new NotFoundException(
          String.format("No calcom team associated to agency with id: %s", agencyId));
    }

  }


}
