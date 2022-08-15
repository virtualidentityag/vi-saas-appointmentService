package com.vi.appointmentservice.api.facade;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vi.appointmentservice.api.exception.httpresponses.BadRequestException;
import com.vi.appointmentservice.api.exception.httpresponses.CalComApiErrorException;
import com.vi.appointmentservice.api.exception.httpresponses.InternalServerErrorException;
import com.vi.appointmentservice.api.model.AgencyAdminResponseDTO;
import com.vi.appointmentservice.api.model.CalcomBooking;
import com.vi.appointmentservice.api.model.CalcomEventTypeDTO;
import com.vi.appointmentservice.api.model.CalcomEventTypeDTOLocationsInner;
import com.vi.appointmentservice.api.model.CalcomUser;
import com.vi.appointmentservice.api.model.ConsultantDTO;
import com.vi.appointmentservice.api.model.MeetingSlug;
import com.vi.appointmentservice.api.service.calcom.CalComAvailabilityService;
import com.vi.appointmentservice.api.service.calcom.CalComBookingService;
import com.vi.appointmentservice.api.service.calcom.CalComEventTypeService;
import com.vi.appointmentservice.api.service.calcom.CalComMembershipService;
import com.vi.appointmentservice.api.service.calcom.CalComScheduleService;
import com.vi.appointmentservice.api.service.calcom.CalComUserService;
import com.vi.appointmentservice.api.service.onlineberatung.UserService;
import com.vi.appointmentservice.model.CalcomUserToConsultant;
import com.vi.appointmentservice.repository.CalcomUserToConsultantRepository;
import com.vi.appointmentservice.repository.EventTypeRepository;
import com.vi.appointmentservice.repository.ScheduleRepository;
import com.vi.appointmentservice.repository.TeamToAgencyRepository;
import com.vi.appointmentservice.repository.UserRepository;
import com.vi.appointmentservice.repository.WebhookRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class ConsultantFacade {
  private final @NonNull CalComUserService calComUserService;
  private final @NonNull CalComEventTypeService calComEventTypeService;
  private final @NonNull CalComScheduleService calComScheduleService;
  private final @NonNull CalComAvailabilityService calComAvailabilityService;
  private final @NonNull CalComMembershipService calComMembershipService;
  private final @NonNull CalComBookingService calComBookingService;
  private final @NonNull UserService userService;
  private final @NonNull CalcomUserToConsultantRepository calcomUserToConsultantRepository;
  private final @NonNull TeamToAgencyRepository teamToAgencyRepository;
  private final @NonNull UserRepository userRepository;
  private final @NonNull ScheduleRepository scheduleRepository;
  private final @NonNull EventTypeRepository eventTypeRepository;
  private final @NonNull WebhookRepository webhookRepository;

  @Value("${app.base.url}")
  private String appBaseUrl;

  public List<CalcomUser> initializeConsultantsHandler() {
    ObjectMapper objectMapper = new ObjectMapper();
    List<CalcomUser> createdOrUpdatedUsers = new ArrayList<>();
    JSONArray consultantsArray = this.userService.getAllConsultants();
    for (int i = 0; i < consultantsArray.length(); i++) {
      ConsultantDTO consultant;
      try {
        consultant = objectMapper.readValue(consultantsArray.getJSONObject(i).toString(),
            ConsultantDTO.class);
      } catch (JsonProcessingException e) {
        throw new CalComApiErrorException(
            "Could not deserialize ConsultantDTO response from userService");
      }
      CalcomUser createdUser = this.createOrUpdateCalcomUserHandler(consultant);
      if(createdUser != null){
        createdOrUpdatedUsers.add(createdUser);
      }
    }
    log.info("Consultants created or updated in sync: {}", createdOrUpdatedUsers);
    return createdOrUpdatedUsers;
  }

  private void createUserIdAssociation(CalcomUser createdUser, ConsultantDTO consultant) {
    calcomUserToConsultantRepository.save(
        new CalcomUserToConsultant(consultant.getId(), createdUser.getId()));
  }

  private CalcomUser createCalcomUser(ConsultantDTO consultant) {
    if (calcomUserToConsultantRepository.existsByConsultantId(consultant.getId())) {
      // Already exists, update user
      return this.updateCalcomUser(consultant);
    } else {
      CalcomUser creationUser = new CalcomUser();
      creationUser.setName(consultant.getFirstname() + " " + consultant.getLastname());
      creationUser.setEmail(consultant.getEmail());
      // Default values
      creationUser.setTimeZone("Europe/Berlin");
      creationUser.setWeekStart("Monday");
      creationUser.setLocale("de");
      creationUser.setTimeFormat(24);
      creationUser.setAllowDynamicBooking(false);
      creationUser.setAway(consultant.getAbsent());
      ObjectMapper objectMapper = new ObjectMapper();
      // Ignore null values
      objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
      JSONObject userPayloadJson = null;
      try {
        userPayloadJson = new JSONObject(objectMapper.writeValueAsString(creationUser));
      } catch (JsonProcessingException e) {
        throw new InternalServerErrorException("Could not serialize createCalcomUser payload");
      }
      // Create association
      CalcomUser createdUser = calComUserService.createUser(userPayloadJson);
      userRepository.initUserAddExtraData(createdUser.getId());
      return calComUserService.getUserById(createdUser.getId());
    }
  }

  public CalcomUser createOrUpdateCalcomUserHandler(ConsultantDTO consultant) {
    //TODO: remove inline comments
    CalcomUser createdOrUpdatedUser = null;
    log.info("Creating or updating consultant: {}", consultant);
    if (calcomUserToConsultantRepository.existsByConsultantId(consultant.getId())) {
      // Found user association
      Long calComUserId = calcomUserToConsultantRepository.findByConsultantId(consultant.getId())
          .getCalComUserId();
      // Check if user really exists
      CalcomUser foundUser = calComUserService.getUserById(calComUserId);
      if (foundUser == null) {
        // TODO: Check calcom for email match?
        // Association exists, user missing, create
        createdOrUpdatedUser = this.createCalcomUser(consultant);
      } else {
        // User exists, update
        createdOrUpdatedUser = this.updateCalcomUser(consultant);
      }
      if (createdOrUpdatedUser != null) {
        updateUserDefaultEntities(createdOrUpdatedUser);
      }
    } else {
      // NO association exists, create it and user
      // TODO: Check calcom for email match?
      createdOrUpdatedUser = this.createCalcomUser(consultant);
      if (createdOrUpdatedUser != null) {
        this.createUserIdAssociation(createdOrUpdatedUser, consultant);
        updateUserDefaultEntities(createdOrUpdatedUser);
      }
    }
    return createdOrUpdatedUser;
  }

  void updateUserDefaultEntities(CalcomUser createdOrUpdatedUser) {
      webhookRepository.updateUserWebhook(createdOrUpdatedUser.getId());
      Long defaultScheduleId = scheduleRepository.createDefaultScheduleIfNoneExists(
          createdOrUpdatedUser.getId());
      // Add default eventTypes
      if (calComEventTypeService.getAllEventTypesOfUser(createdOrUpdatedUser.getId()).isEmpty()) {
        Long newDefaultEventTypeId = addDefaultEventTypeToUser(createdOrUpdatedUser);
        eventTypeRepository.updateEventTypeScheduleId(newDefaultEventTypeId, defaultScheduleId);
      }
      // Add user to teams of agencies and event-types of teams
      // this.addUserToTeamsAndEventTypes(consultant);
  }

  private Long addDefaultEventTypeToUser(CalcomUser createdUser) {
    ObjectMapper objectMapper = new ObjectMapper();
    // Ignore null values
    objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    CalcomEventTypeDTO eventType = this.getDefaultCalcomEventType(createdUser);
    JSONObject eventTypeJson;
    try {
      eventTypeJson = new JSONObject(objectMapper.writeValueAsString(eventType));
    } catch (JsonProcessingException e) {
      throw new CalComApiErrorException("Could not serialize default event-type");
    }
    return Long.valueOf(calComEventTypeService.createEventType(eventTypeJson).getId());

  }

  private void addUserToTeamsAndEventTypes(ConsultantDTO consultant) {
    // Add user to team of teams of agencies
    List<AgencyAdminResponseDTO> agencies = consultant.getAgencies();
    for (AgencyAdminResponseDTO agency : agencies) {
      Long teamId;
      if (teamToAgencyRepository.existsByAgencyId(agency.getId())) {
        teamId = teamToAgencyRepository.findByAgencyId(agency.getId()).get().getTeamid();
      } else {
        // TODO: Create team for agency
        // teamId = newlyCreatedTeam.getId();
        // TODO: Create eventType for agency
      }
      // TODO: Associate user to team+
      // TODO: Check event-type
      // TODO: Associate user to team event-types?
      // TODO: Check if membership already exists (calcom route currently limited to memerships of api key creator)
      // TODO: calComTeamService.addUserToTeam(updatedUser.getId(), teamId);
    }
  }

  private CalcomEventTypeDTO getDefaultCalcomEventType(CalcomUser createdUser) {
    CalcomEventTypeDTO eventType = new CalcomEventTypeDTO();
    eventType.setUserId(Math.toIntExact(createdUser.getId()));
    eventType.setTitle("Beratung mit " + createdUser.getName());
    eventType.setSlug(UUID.randomUUID().toString());
    eventType.setLength(60);
    eventType.setHidden(false);
    eventType.setEventName("Beratung {ATTENDEE} mit {HOST}");
    eventType.setRequiresConfirmation(false);
    eventType.setDisableGuests(true);
    eventType.setHideCalendarNotes(true);
    eventType.setMinimumBookingNotice(120);
    eventType.setBeforeEventBuffer(0);
    eventType.setAfterEventBuffer(0);
    // eventType.setSuccessRedirectUrl(appBaseUrl + "/sessions/user/view/");
    eventType.setDescription("");
    List<CalcomEventTypeDTOLocationsInner> locations = new ArrayList<>();
    CalcomEventTypeDTOLocationsInner location = new CalcomEventTypeDTOLocationsInner();
    location.setType("integrations:daily");
    locations.add(location);
    eventType.setLocations(locations);
    return eventType;
  }

  private CalcomUser updateCalcomUser(ConsultantDTO consultant) {
    if (calcomUserToConsultantRepository.existsByConsultantId(consultant.getId())) {
      // Exists, update the user
      Long calcomUserId = calcomUserToConsultantRepository.findByConsultantId(consultant.getId())
          .getCalComUserId();
      CalcomUser updateUser = new CalcomUser();
      updateUser.setId(calcomUserId);
      updateUser.setName(consultant.getFirstname() + " " + consultant.getLastname());
      updateUser.setEmail(consultant.getEmail());
      // Default values
      updateUser.setTimeZone("Europe/Berlin");
      updateUser.setWeekStart("Monday");
      updateUser.setLocale("de");
      updateUser.setTimeFormat(24);
      updateUser.setAllowDynamicBooking(false);
      updateUser.setAway(consultant.getAbsent());
      // TODO: Any more default values?
      userRepository.updateUser(updateUser);
      return calComUserService.getUserById(updateUser.getId());
    } else {
      // Doesn't exist, create the user
      return this.createCalcomUser(consultant);
    }
  }

  public HttpStatus deleteConsultantHandler(String consultantId) {
    checkIfConsultantExists(consultantId);
    // Find associated user
    Long calcomUserId = calcomUserToConsultantRepository.findByConsultantId(consultantId)
        .getCalComUserId();
    // Delete team memberships
    calComMembershipService.deleteAllMembershipsOfUser(calcomUserId);
    // Delete personal event-types
    calComEventTypeService.deleteAllEventTypesOfUser(calcomUserId);
    // Delete schedules
    List<Integer> deletedSchedules = calComScheduleService.deleteAllSchedulesOfUser(calcomUserId);
    // Delete availabilities for schedules
    for (Integer scheduleId : deletedSchedules) {
      calComAvailabilityService.deleteAvailability(scheduleId);
    }
    // Delete user
    HttpStatus deleteResponseCode = calComUserService.deleteUser(calcomUserId);
    // Remove association
    if (deleteResponseCode == HttpStatus.OK) {
      calcomUserToConsultantRepository.deleteByConsultantId(consultantId);
    }
    // TODO: ANYTHING ELSE?
    return deleteResponseCode;

  }

  public List<CalcomBooking> getConsultantActiveBookings(String consultantId) {
    checkIfConsultantExists(consultantId);
    Long calcomUserId = calcomUserToConsultantRepository.findByConsultantId(consultantId)
        .getCalComUserId();
    return calComBookingService.getConsultantActiveBookings(calcomUserId);
  }

  public List<CalcomBooking> getConsultantCancelledBookings(String consultantId) {
    checkIfConsultantExists(consultantId);
    Long calcomUserId = calcomUserToConsultantRepository.findByConsultantId(consultantId)
        .getCalComUserId();
    return calComBookingService.getConsultantCancelledBookings(calcomUserId);
  }

  public List<CalcomBooking> getConsultantExpiredBookings(String consultantId) {
    checkIfConsultantExists(consultantId);
    Long calcomUserId = calcomUserToConsultantRepository.findByConsultantId(consultantId)
        .getCalComUserId();
    return calComBookingService.getConsultantExpiredBookings(calcomUserId);
  }

  public List<CalcomEventTypeDTO> getAllEventTypesOfConsultantHandler(String consultantId) {
    checkIfConsultantExists(consultantId);
    return calComEventTypeService.getAllEventTypesOfUser(
        calcomUserToConsultantRepository.findByConsultantId(consultantId).getCalComUserId());
  }

  public MeetingSlug getConsultantMeetingSlugHandler(String consultantId) {
    checkIfConsultantExists(consultantId);
    MeetingSlug meetingSlug = new MeetingSlug();
    meetingSlug.setSlug(calComUserService.getUserById(
        calcomUserToConsultantRepository.findByConsultantId(consultantId).getCalComUserId())
        .getUsername());
    return meetingSlug;
  }

  private void checkIfConsultantExists(String consultantId) {
    if (!calcomUserToConsultantRepository.existsByConsultantId(consultantId)) {
      throw new BadRequestException(
          String.format("No calcom user associated to consultant id '%s'", consultantId));
    }
  }

}
