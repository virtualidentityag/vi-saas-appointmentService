package com.vi.appointmentservice.controller;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vi.appointmentservice.api.model.*;
import com.vi.appointmentservice.generated.api.controller.ConsultantsApi;
import com.vi.appointmentservice.model.CalcomUserToConsultant;
import com.vi.appointmentservice.repository.CalcomUserToConsultantRepository;
import com.vi.appointmentservice.repository.TeamToAgencyRepository;
import com.vi.appointmentservice.service.CalComBookingService;
import com.vi.appointmentservice.service.CalComEventTypeService;
import com.vi.appointmentservice.service.CalComTeamService;
import com.vi.appointmentservice.service.CalComUserService;
import com.vi.appointmentservice.service.UserService;
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
        log.info("updateConsultants");
        ObjectMapper objectMapper = new ObjectMapper();
        JSONArray consultantsArray = this.userService.getAllConsultants();
        JSONObject response = new JSONObject();
        List<CalcomUser> createdList = new ArrayList<>();
        List<CalcomUser> updatedList = new ArrayList<>();
        List<ConsultantDTO> skippedList = new ArrayList<>();
        CalcomUser createdUser;
        for (int i = 0; i < consultantsArray.length(); i++) {
            createdUser = null;
            ConsultantDTO consultant = objectMapper.readValue(consultantsArray.getJSONObject(i).toString(), ConsultantDTO.class);
            // TODO: Check calcom for email match
            if (calcomUserToConsultantRepository.existsByConsultantId(consultant.getId())) {
                // Found user association
                Long calComUserId = calcomUserToConsultantRepository.findByConsultantId(consultant.getId()).getCalComUserId();
                // Check if user really exists
                CalcomUser foundUser = calComUserService.getUserById(calComUserId);
                if (foundUser == null) {
                    // User missing, create
                    createdUser = this.createCalcomUser(consultant);
                    if (createdUser != null) {
                        createdList.add(createdUser);
                    } else {
                        skippedList.add(consultant);
                    }
                } else {
                    // User exists, update
                    // TODO: User PATCH API of calcom currently buggy and only updates the user that created the API Key
                    // createdUser = this.updateCalcomUser(consultant);
                    if (createdUser != null) {
                        updatedList.add(createdUser);
                    } else {
                        skippedList.add(consultant);
                    }
                }
            } else {
                // TODO: email already exists in calcom?
                createdUser = this.createCalcomUser(consultant);
                if (createdUser != null) {
                    createdList.add(createdUser);
                } else {
                    skippedList.add(consultant);
                }
            }
        }
        response.put("created", new JSONArray(createdList));
        response.put("updated", new JSONArray(updatedList));
        response.put("skipped", new JSONArray(skippedList));
        log.info("Consultant Sync: {}", response);
        return new ResponseEntity<>(response.toString(), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<CalcomUser> createConsultant(ConsultantDTO consultant) {
        try {
            CalcomUser createdUser = this.createCalcomUser(consultant);
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
            CalcomUser createdUser = calComUserService.createUser(createJson);
            // Create association
            if (createdUser != null) {
                calcomUserToConsultantRepository.save(new CalcomUserToConsultant(consultant.getId(), createdUser.getId()));
                // Add user to team of teams of agencies
                List<AgencyAdminResponseDTO> agencies = consultant.getAgencies();
                for (AgencyAdminResponseDTO agency : agencies) {
                    Long teamId;
                    if (!teamToAgencyRepository.existsByAgencyId(agency.getId())) {
                        // TODO: Create team for agency
                        // teamId = newlyCreatedTeam.getId();
                        // TODO: Create eventType for agency
                    } else {
                        teamId = teamToAgencyRepository.findByAgencyId(agency.getId()).get(0).getTeamid();
                    }
                    // TODO: Associate user to team+
                    // TODO: Check event-type
                    // TODO: Associate user to event-type?
                    // TODO: Check if membership already exists (calcom route currently limited to memerships of api key creator)
                    // TODO: calComTeamService.addUserToTeam(updatedUser.getId(), teamId);
                }
                return createdUser;
            } else {
                return null;
            }
        }
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
            CalcomUser updatedUser = calComUserService.updateUser(updateJson);
            // Add user to team of teams of agencies
            if (updatedUser != null) {
                List<AgencyAdminResponseDTO> agencies = consultant.getAgencies();
                for (AgencyAdminResponseDTO agency : agencies) {
                    Long teamId;
                    if (!teamToAgencyRepository.existsByAgencyId(agency.getId())) {
                        // TODO: Create team for agency
                        // teamId = newlyCreatedTeam.getId();
                        // TODO: Create eventType for agency
                    } else {
                        teamId = teamToAgencyRepository.findByAgencyId(agency.getId()).get(0).getTeamid();
                    }
                    // TODO: Associate user to team+
                    // TODO: Check event-type
                    // TODO: Associate user to event-type?
                    // TODO: Check if membership already exists (calcom route currently limited to memerships of api key creator)
                    // TODO: calComTeamService.addUserToTeam(updatedUser.getId(), teamId);
                }
                return updatedUser;
            } else {
                return null;

            }
        } else {
            // Doesnt exists, create the user
            return this.createCalcomUser(consultant);

        }
    }

    @Override
    public ResponseEntity<Void> deleteConsultant(String consultantId) {
        Long calcomUserId = calcomUserToConsultantRepository.findByConsultantId(consultantId).getCalComUserId();
        HttpStatus responseCode = calComUserService.deleteUser(calcomUserId);
        if (responseCode == HttpStatus.OK) {
            calcomUserToConsultantRepository.deleteByConsultantId(consultantId);
        }
        return new ResponseEntity<>(responseCode);
    }

    @Override
    public ResponseEntity<CalcomUser> updateConsultant(String userId, ConsultantDTO consultantDTO) {
        return ConsultantsApi.super.updateConsultant(userId, consultantDTO);
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
