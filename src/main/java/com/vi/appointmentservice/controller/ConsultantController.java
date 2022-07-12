package com.vi.appointmentservice.controller;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vi.appointmentservice.api.model.*;
import com.vi.appointmentservice.generated.api.controller.ConsultantsApi;
import com.vi.appointmentservice.model.CalcomUserToConsultant;
import com.vi.appointmentservice.repository.CalcomUserToConsultantRepository;
import com.vi.appointmentservice.repository.TeamToAgencyRepository;
import com.vi.appointmentservice.service.*;
import io.swagger.annotations.Api;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Controller for consultant API operations.
 */
@RestController
@Api(tags = "consultant")
@Slf4j
@RequiredArgsConstructor
public class ConsultantController implements ConsultantsApi {

    private final @NonNull CalComUserService calComUserService;
    private final @NonNull CalComTeamService calComTeamService;
    private final @NonNull CalComEventTypeService calComEventTypeService;
    private final @NonNull CalComBookingService calComBookingService;
    private final @NonNull UserService userService;
    private final @NonNull CalcomUserToConsultantRepository calcomUserToConsultantRepository;
    private final @NonNull TeamToAgencyRepository teamToAgencyRepository;

    @GetMapping(
            value = "/consultants",
            produces = {"application/json"}
    )
    ResponseEntity<String> getAllConsultants() {
        JSONArray consultantsArray = this.userService.getAllConsultants();
        return new ResponseEntity<>(consultantsArray.toString(), HttpStatus.OK);
    }

    @GetMapping(
            value = "/updateConsultants",
            produces = {"application/json"}
    )
    ResponseEntity<String> updateConsultants() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        JSONArray consultantsArray = this.userService.getAllConsultants();
        JSONArray response = new JSONArray();
        for (int i = 0; i < consultantsArray.length(); i++) {
            ConsultantDTO consultant = objectMapper.readValue(consultantsArray.getJSONObject(i).toString(), ConsultantDTO.class);
            CalcomUser createdOrUpdateUser = this.createOrUpdateCalcomUserHandler(consultant);
            if(createdOrUpdateUser != null){
                response.put(new JSONObject(createdOrUpdateUser));
            }
        }
        log.info("Consultant created or updated in sync: {}", response);
        return new ResponseEntity<>(response.toString(), HttpStatus.OK);
    }

    private CalcomUser createOrUpdateCalcomUserHandler(ConsultantDTO consultant) throws JsonProcessingException {
        CalcomUser createdUser = null;
        if (calcomUserToConsultantRepository.existsByConsultantId(consultant.getId())) {
            // Found user association
            Long calComUserId = calcomUserToConsultantRepository.findByConsultantId(consultant.getId()).getCalComUserId();
            // Check if user really exists
            CalcomUser foundUser = calComUserService.getUserById(calComUserId);
            if (foundUser == null) {
                // TODO: Check calcom for email match?
                // User missing, create
                createdUser = this.createCalcomUser(consultant);
                if (createdUser != null) {
                    // Add default event-type to user
                    if (calComEventTypeService.getAllEventTypesOfUser(createdUser.getId()).isEmpty()) {
                        addDefaultEventTypeToUser(createdUser);
                    }
                    // Add user to teams of agencies and event-types of teams
                    this.addUserToTeamsAndEventTypes(consultant);
                }
            } else {
                // User exists, update
                // TODO: User PATCH API of calcom currently buggy and only updates the user that created the API Key
                // createdUser = this.updateCalcomUser(consultant);
                if (createdUser != null) {
                    if (calComEventTypeService.getAllEventTypesOfUser(createdUser.getId()).isEmpty()) {
                        addDefaultEventTypeToUser(createdUser);
                    }
                    // Add user to teams of agencies and event-types of teams
                    this.addUserToTeamsAndEventTypes(consultant);
                }
            }
        } else {
            // TODO: Check calcom for email match?
            createdUser = this.createCalcomUser(consultant);
            if (createdUser != null) {
                // Add association to dataLayer
                this.createUserIdAssociation(createdUser, consultant);
                // Add default eventTypes
                if (calComEventTypeService.getAllEventTypesOfUser(createdUser.getId()).isEmpty()) {
                    addDefaultEventTypeToUser(createdUser);
                }
                // Add user to teams of agencies and event-types of teams
                this.addUserToTeamsAndEventTypes(consultant);
            }
        }
        return createdUser;
    }

    private void createUserIdAssociation(CalcomUser createdUser, ConsultantDTO consultant) {
        calcomUserToConsultantRepository.save(new CalcomUserToConsultant(consultant.getId(), createdUser.getId()));
    }

    @Override
    public ResponseEntity<CalcomUser> createConsultant(ConsultantDTO consultant) {
        try {
            CalcomUser createdUser = createOrUpdateCalcomUserHandler(consultant);
            return new ResponseEntity<>(createdUser, HttpStatus.OK);
        } catch (JsonProcessingException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private CalcomUser createCalcomUser(ConsultantDTO consultant) throws JsonProcessingException {
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
            creationUser.setAway(false);
            // TODO: Any more default values?
            ObjectMapper objectMapper = new ObjectMapper();
            // Ignore null values
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            JSONObject createJson = new JSONObject(objectMapper.writeValueAsString(creationUser));
            // Create association
            return calComUserService.createUser(createJson);
        }
    }

    private void addDefaultEventTypeToUser(CalcomUser createdUser) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        // Ignore null values
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        CalcomEventType eventType = getDefaultCalcomEventType(createdUser);
        JSONObject eventTypeJson = new JSONObject(objectMapper.writeValueAsString(eventType));
        calComEventTypeService.createEventType(eventTypeJson);
    }

    private void addUserToTeamsAndEventTypes(ConsultantDTO consultant) {
        // Add user to team of teams of agencies
        List<AgencyAdminResponseDTO> agencies = consultant.getAgencies();
        for (AgencyAdminResponseDTO agency : agencies) {
            Long teamId;
            if (teamToAgencyRepository.existsByAgencyId(agency.getId())) {
                teamId = teamToAgencyRepository.findByAgencyId(agency.getId()).get(0).getTeamid();
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

    private CalcomEventType getDefaultCalcomEventType(CalcomUser createdUser) {
        CalcomEventType eventType = new CalcomEventType();
        eventType.setUserId(Math.toIntExact(createdUser.getId()));
        eventType.setTitle("Beratung mit Counsellor Name von Name of the agency");
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
        eventType.setSuccessRedirectUrl("https://app-develop.suchtberatung.digital/sessions/user/view/");
        eventType.setDescription("");
        List<CalcomEventTypeLocationsInner> locations = new ArrayList<>();
        CalcomEventTypeLocationsInner location = new CalcomEventTypeLocationsInner();
        location.setType("integrations:daily");
        locations.add(location);
        eventType.setLocations(locations);
        return eventType;
    }

    private CalcomUser updateCalcomUser(ConsultantDTO consultant) throws JsonProcessingException {
        if (calcomUserToConsultantRepository.existsByConsultantId(consultant.getId())) {
            // Exists, update the user
            Long calcomUserId = calcomUserToConsultantRepository.findByConsultantId(consultant.getId()).getCalComUserId();
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
            updateUser.setAway(false);
            // TODO: Any more default values?
            ObjectMapper objectMapper = new ObjectMapper();
            // Ignore null values
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            JSONObject updateJson = new JSONObject(objectMapper.writeValueAsString(updateUser));
            return calComUserService.updateUser(updateJson);
        } else {
            // Doesnt exists, create the user
            return this.createCalcomUser(consultant);

        }
    }


    @Override
    public ResponseEntity<Void> deleteConsultant(String consultantId) {
        // return new ResponseEntity<>(this.delteConsultantHandler(consultantId));
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    private HttpStatus delteConsultantHandler(String consultantId){
        if(calcomUserToConsultantRepository.existsByConsultantId(consultantId)) {
            // Find associated user
            Long calcomUserId = calcomUserToConsultantRepository.findByConsultantId(consultantId).getCalComUserId();
            // TODO: DELETE TEAM AND EVENT TYPE MEMBERSHIPS
            // TODO: DELETE PERSONAL EVENT-TYPES
            // TODO: DELETE SCHEDULES
            // TODO: DELETE AVAILABILITIES
            // TODO: CANCEL BOOKINGS
            // Delete user
            HttpStatus deleteResponseCode = calComUserService.deleteUser(calcomUserId);
            // Remove association
            if (deleteResponseCode == HttpStatus.OK) {
                calcomUserToConsultantRepository.deleteByConsultantId(consultantId);
            }
            // TODO: ANYTHING ELSE?
            return deleteResponseCode;
        }
        return null;
    }

    @Override
    public ResponseEntity<CalcomUser> updateConsultant(String consultantId, ConsultantDTO consultant) {
        if(Objects.equals(consultantId, consultant.getId())){
            try {
                CalcomUser createdUser = createOrUpdateCalcomUserHandler(consultant);
                return new ResponseEntity<>(createdUser, HttpStatus.OK);
            } catch (JsonProcessingException e) {
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }else{
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public ResponseEntity<CalcomEventType> addEventTypeToConsultant(String userId, CalcomEventType calcomEventType) {
        return ConsultantsApi.super.addEventTypeToConsultant(userId, calcomEventType);
    }


    @Override
    public ResponseEntity<List<CalcomBooking>> getAllBookingsOfConsultant(String userId) {
        try {
            Integer calcomUserId = Math.toIntExact(calcomUserToConsultantRepository.findByConsultantId(userId).getCalComUserId());

            List<CalcomBooking> bookings = calComBookingService.getBookings().stream()
                    .filter(o -> o.getUserId().equals(calcomUserId))
                    .collect(Collectors.toList());

            return new ResponseEntity<>(bookings, HttpStatus.OK);
        } catch (Exception e) {
            log.error(e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<List<CalcomEventType>> getAllEventTypesOfConsultant(String userId) {
        if (calcomUserToConsultantRepository.existsByConsultantId(userId)) {
            List<CalcomEventType> eventTypes;
            try {
                eventTypes = calComEventTypeService.getAllEventTypesOfUser(calcomUserToConsultantRepository.findByConsultantId(userId).getCalComUserId());
            } catch (JsonProcessingException e) {
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
            return new ResponseEntity<>(eventTypes, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @Override
    public ResponseEntity<MeetingSlug> getConsultantMeetingSlug(String userId) {
        if (calcomUserToConsultantRepository.existsByConsultantId(userId)) {
            MeetingSlug meetingSlug = new MeetingSlug();
            try {
                meetingSlug.setSlug(calComUserService.getUserById(calcomUserToConsultantRepository.findByConsultantId(userId).getCalComUserId()).getUsername());
            } catch (JsonProcessingException e) {
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
            return new ResponseEntity<>(meetingSlug, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

}
