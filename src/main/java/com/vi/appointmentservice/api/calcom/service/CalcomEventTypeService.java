package com.vi.appointmentservice.api.calcom.service;

import com.vi.appointmentservice.api.calcom.model.CalcomEventType;
import com.vi.appointmentservice.api.calcom.repository.EventTypeRepository;
import com.vi.appointmentservice.api.calcom.repository.MembershipsRepository;
import com.vi.appointmentservice.api.calcom.repository.WebhookRepository;
import com.vi.appointmentservice.api.facade.AppointmentType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
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

  public CalcomEventType createEventType(Number teamId, AppointmentType appointmentType) {
    CalcomEventType eventType = this.buildEventType(appointmentType);
    eventType.setTitle(appointmentType.getTitle());
    eventType.setTeamId(teamId);
    CalcomEventType eventTypeDB = eventTypeRepository.createEventType(eventType);
    eventTypeRepository.markAsRoundRobin(eventTypeDB.getId());
    return eventTypeDB;
  }

  public void markAsDefaultEventType(CalcomEventType eventType) {
    eventTypeRepository.markAsDefaultEventType(eventType.getId());
  }

  public void createEventType(com.vi.appointmentservice.api.calcom.model.CalcomUser calcomUser,
      AppointmentType appointmentType,
      Long defaultScheduleId) {
    webhookRepository.updateUserWebhook(calcomUser.getId());
    CalcomEventType eventType = this.buildEventType(appointmentType);
    eventType.setTitle(appointmentType.getTitle() + " " + calcomUser.getName());
    eventType.setUserId(Math.toIntExact(calcomUser.getId()));
    CalcomEventType createdEventType = eventTypeRepository.createEventType(eventType);
    eventTypeRepository.addUserEventTypeRelation(createdEventType.getId(), calcomUser.getId());
    //TODO: this can be one call to DB
    eventTypeRepository.updateEventTypeScheduleId(eventType.getId(), defaultScheduleId);
  }

  public CalcomEventType buildEventType(AppointmentType appointmentType) {
    CalcomEventType eventType = new CalcomEventType();
    eventType.setDescription(appointmentType.getDescription());
    eventType.setLength(appointmentType.getLength());
    eventType.setMinimumBookingNotice(appointmentType.getMinimumBookingNotice());
    eventType.setBeforeEventBuffer(appointmentType.getBeforeEventBuffer());
    eventType.setAfterEventBuffer(appointmentType.getAfterEventBuffer());
    eventType.setSlotInterval(appointmentType.getSlotInterval());
    eventType.setSlug(UUID.randomUUID().toString());
    eventType.setEventName(appointmentType.getTitle() + " {ATTENDEE} mit {HOST}");
    eventType.setRequiresConfirmation(false);
    eventType.setDisableGuests(true);
    eventType.setHideCalendarNotes(true);
    eventType.setPeriodDays(30);
    eventType.setLocations(calcomLocationsService.buildCalcomLocations());
    return eventType;
  }

  public CalcomEventType updateEventType(CalcomEventType eventType) {
    eventTypeRepository.updateEventType(eventType);
    eventTypeRepository.removeTeamEventTypeMembershipsForEventType(eventType.getId());
    eventType.getMemberIds().forEach(calcomUser -> addUser2Event(calcomUser, eventType.getId()));
    return getEventTypeById(eventType.getId());
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
    eventTypeRepository.removeTeamEventTypeMembershipsForUser(
        calcomUserId, teamIds);
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
  }
}
