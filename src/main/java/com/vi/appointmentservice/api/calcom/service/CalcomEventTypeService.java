package com.vi.appointmentservice.api.calcom.service;

import com.google.common.base.Joiner;
import com.vi.appointmentservice.api.calcom.model.CalcomEventType;
import com.vi.appointmentservice.api.calcom.repository.EventTypeRepository;
import com.vi.appointmentservice.api.calcom.repository.MembershipsRepository;
import com.vi.appointmentservice.api.calcom.repository.WebhookRepository;
import com.vi.appointmentservice.api.facade.AppointmentType;
import com.vi.appointmentservice.api.facade.DefaultTextConstants;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CalcomEventTypeService {

  @NonNull
  private final EventTypeRepository eventTypeRepository;

  private final MembershipsRepository membershipsRepository;

  private final @NonNull WebhookRepository webhookRepository;

  private final @NonNull CalcomLocationsService calcomLocationsService;

  @Value("${app.base.url}")
  private String appBaseUrl;


  public CalcomEventType getEventTypeById(Number eventTypeId) {
    var eventType = eventTypeRepository.getEventTypeById(eventTypeId);
    List<Long> userIdsOfEventTypeMembers = eventTypeRepository
        .getUserIdsOfEventTypeMembers(eventType.getId());
    eventType.setMemberIds(userIdsOfEventTypeMembers);
    return eventType;
  }

  public Optional<CalcomEventType> findEventTypeById(Number eventTypeId) {
    return eventTypeRepository.findEventTypeById(eventTypeId);
  }

  public CalcomEventType getEventTypeByUserId(Number userId) {
    return eventTypeRepository.getEventTypeByUserId(userId);
  }

  public CalcomEventType createEventType(Number teamId, AppointmentType appointmentType) {
    CalcomEventType eventType = this.buildDefaultEventType(appointmentType);
    eventType.setTitle(appointmentType.getTitle());
    eventType.setTeamId(teamId);
    CalcomEventType eventTypeDB = eventTypeRepository.createEventType(eventType);
    if (appointmentType.getLocations() != null) {
      updateEventTypeLocations(eventTypeDB, appointmentType.getLocations());
    }
    eventTypeRepository.markAsRoundRobin(eventTypeDB.getId());
    return eventTypeDB;
  }

  public void createEventType(com.vi.appointmentservice.api.calcom.model.CalcomUser calcomUser,
      AppointmentType appointmentType,
      Long defaultScheduleId) {
    webhookRepository.updateUserWebhook(calcomUser.getId());
    CalcomEventType eventType = this.buildDefaultEventType(appointmentType);
    eventType.setTitle(appointmentType.getTitle() + " " + calcomUser.getName());
    eventType.setUserId(Math.toIntExact(calcomUser.getId()));
    CalcomEventType createdEventType = eventTypeRepository.createEventType(eventType);
    eventTypeRepository.addUserEventTypeRelation(createdEventType.getId(), calcomUser.getId());
    eventTypeRepository.updateEventTypeScheduleId(createdEventType.getId(), defaultScheduleId);
  }

  public CalcomEventType buildDefaultEventType(AppointmentType appointmentType) {
    CalcomEventType eventType = new CalcomEventType();
    eventType.setDescription(appointmentType.getDescription());
    eventType.setLength(appointmentType.getLength());
    eventType.setMinimumBookingNotice(appointmentType.getMinimumBookingNotice());
    eventType.setBeforeEventBuffer(appointmentType.getBeforeEventBuffer());
    eventType.setAfterEventBuffer(appointmentType.getAfterEventBuffer());
    eventType.setSlotInterval(appointmentType.getSlotInterval());
    eventType.setSlug(UUID.randomUUID().toString());
    eventType.setEventName("Beratung zwischen dem / der Berater:in {HOST} und dem / der Ratsuchenden {ATTENDEE}");
    eventType.setRequiresConfirmation(false);
    eventType.setDisableGuests(true);
    eventType.setHideCalendarNotes(true);
    eventType.setPeriodDays(30);
    eventType.setLocations(calcomLocationsService.buildDefaultCalcomLocations());
    return eventType;
  }

  public CalcomEventType updateEventType(CalcomEventType eventType) {
    eventTypeRepository.updateEventType(eventType);
    eventTypeRepository.removeTeamEventTypeMembershipsForEventType(eventType.getId());
    eventTypeRepository.removeTeamEventHostsForEventType(eventType.getId());
    eventType.getMemberIds().forEach(calcomUser -> addUser2Event(calcomUser, eventType.getId()));
    return getEventTypeById(eventType.getId());
  }

  public CalcomEventType updateEventType(CalcomEventType eventTypeDB, List<String> locations) {
    if (locations != null) {
      updateEventTypeLocations(eventTypeDB, locations);
    }
    return this.updateEventType(eventTypeDB);
  }

  private void updateEventTypeLocations(CalcomEventType eventTypeDB, List<String> locations) {
    List<String> locationJsons = locations.stream()
        .map(calcomLocationsService::resolveToJsonByLocationType).collect(
            Collectors.toList());
    eventTypeDB.setLocations("[" + Joiner.on(",").join(locationJsons) + "]");
    eventTypeRepository.updateLocations(eventTypeDB.getId(), eventTypeDB.getLocations());
  }

  public void markAsDefaultEventType(CalcomEventType eventType) {
    eventTypeRepository.markAsDefaultEventType(eventType.getId());
  }

  public CalcomEventType getDefaultEventTypeOfTeam(Long teamId) {
    List<CalcomEventType> eventTypes4Team = eventTypeRepository.getEventTypes4Team(teamId);
    Optional<CalcomEventType> defaultEventType = eventTypes4Team.stream().filter(eventType ->
        eventType.getMetadata().contains("defaultEventType")
    ).findFirst();

    if (defaultEventType.isEmpty()) {
      throw new IllegalStateException("No default team event type found for team: " + teamId);
    }

    return defaultEventType.get();
  }


  public void cleanUserMemberships(Long calcomUserId, List<Long> teamIds) {
    if (teamIds != null && !teamIds.isEmpty()) {
      eventTypeRepository.removeTeamEventTypeMembershipsForUser(
          calcomUserId, teamIds);
    } else {
      log.warn("Could not clean user memberships for user: " + calcomUserId + " because teamIds is empty or null ");
    }
  }

  public void addUser2Team(Long calComUserId, Long teamId) {
    eventTypeRepository.removeTeamEventTypeHostsForUser(
        calComUserId);
    CalcomEventType eventType = getDefaultEventTypeOfTeam(teamId);
    eventTypeRepository.addUserEventTypeRelation(Long.valueOf(eventType.getId()), calComUserId);
    eventTypeRepository.addRoundRobinHosts(eventType.getId(), calComUserId);
    membershipsRepository.updateMemberShipsOfUser(calComUserId, teamId);
  }

  public void addUser2Event(Long calComUserId, Number eventTypeId) {
    eventTypeRepository.removeTeamEventTypeHostsForUser(
        calComUserId);
    eventTypeRepository.addUserEventTypeRelation(eventTypeId, calComUserId);
    eventTypeRepository.addRoundRobinHosts(eventTypeId, calComUserId);
  }

  public List<CalcomEventType> getAllEventTypesOfTeam(Long teamid) {
    var eventTypes = eventTypeRepository.getEventTypes4Team(teamid);
    eventTypes.forEach(eventType -> {
      List<Long> userIdsOfEventTypeMembers = eventTypeRepository
          .getUserIdsOfEventTypeMembers(eventType.getId());
      eventType.setMemberIds(userIdsOfEventTypeMembers);
    });
    return eventTypes;
  }

  public void deleteAllEventTypesOfUser(Long calcomUserId) {
    eventTypeRepository.deleteAllEventTypesOfUser(calcomUserId);
  }

  public void deleteEventType(Long eventTypeId) {
    eventTypeRepository.deleteEventType(eventTypeId);
    eventTypeRepository.removeTeamEventTypeMembershipsForEventType(eventTypeId);
    eventTypeRepository.removeTeamEventHostsForEventType(eventTypeId);
  }

  public void updateEventTypeTitle(Long calComUserId, String displayName) {
    CalcomEventType eventTypeByUserId = getEventTypeByUserId(calComUserId);
    if (eventTypeByUserId.getTitle().contains(DefaultTextConstants.BERATUNG_MIT)) {
      eventTypeByUserId.setTitle(DefaultTextConstants.BERATUNG_MIT_DEM_DER_BERATER_IN + " " + displayName);
      eventTypeRepository.updateEventType(eventTypeByUserId);
    } else {
      log.warn("Skipping update of EventType because event type for the user {} does not contain text {}", calComUserId, DefaultTextConstants.BERATUNG_MIT_DEM_DER_BERATER_IN);
    }
  }
}
