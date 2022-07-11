package com.vi.appointmentservice.controller;

import com.vi.appointmentservice.api.model.*;
import com.vi.appointmentservice.generated.api.controller.ConsultantsApi;
import com.vi.appointmentservice.model.CalcomUserToConsultant;
import com.vi.appointmentservice.repository.CalcomUserToConsultantRepository;
import com.vi.appointmentservice.repository.TeamToAgencyRepository;
import com.vi.appointmentservice.service.CalComBookingService;
import com.vi.appointmentservice.service.CalComTeamService;
import com.vi.appointmentservice.service.CalComUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller for consultant API operations.
 */
@RestController
@Api(tags = "consultant")
@Slf4j
public class ConsultantController implements ConsultantsApi {

    CalComUserService calComUserService;
    CalComBookingService calComBookingService;
    CalComTeamService calComTeamService;

    CalcomUserToConsultantRepository calcomUserToConsultantRepository;
    TeamToAgencyRepository teamToAgencyRepository;


    @Autowired
    public ConsultantController(
            CalComUserService calComUserService,
            CalComBookingService calComBookingService,
            CalComTeamService calComTeamService,
            CalcomUserToConsultantRepository calcomUserToConsultantRepository,
            TeamToAgencyRepository teamToAgencyRepository)
    {
        this.calComUserService = calComUserService;
        this.calComBookingService = calComBookingService;
        this.calComTeamService = calComTeamService;
        this.calcomUserToConsultantRepository = calcomUserToConsultantRepository;
        this.teamToAgencyRepository = teamToAgencyRepository;
    }

    /**
     * TEMP Admin route to associate consultant to calcomUser
     *
     * @param consultantId
     * @param requestBodyString
     * @return
     */
    @PostMapping(
            value = "/consultant/{consultantId}/associateUser",
            produces = {"application/json"},
            consumes = {"application/json"}
    )
    ResponseEntity<CalcomUserToConsultant> associateConsultant(@ApiParam(value = "ID of onber user", required = true) @PathVariable("consultantId") String consultantId, @RequestBody String requestBodyString) {
        try {
            JSONObject requestBody = new JSONObject(requestBodyString);
            if (consultantId != null && !consultantId.isEmpty() && !requestBody.isNull("calcomUserId")) {
                CalcomUserToConsultant userAssociation = new CalcomUserToConsultant(consultantId, requestBody.getLong("calcomUserId"));
                return new ResponseEntity<>(calcomUserToConsultantRepository.save(userAssociation), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<CalcomUser> createConsultant(UserDTO userDTO) {
        CalcomUser creationUser = new CalcomUser();
        creationUser.setName(userDTO.getUsername());
        creationUser.setUsername(userDTO.getUsername());
        // TODO: creationUser.setEmail();
        // Default values
        creationUser.setTimeZone("Europe/Berlin");
        creationUser.setWeekStart("Monday");
        creationUser.setLocale("de");
        creationUser.setTimeFormat(24);
        creationUser.setAllowDynamicBooking(false);
        // TODO: Any more default values?

        CalcomUser createdUser = calComUserService.createUser(creationUser);
        // TODO: Add onber userID
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
        // CalcomUserToUser userAssociation = new CalcomUserToUser(userDTO.getUserId(), createdUser.getId());

        // Add user to team of agency
        //Long teamId = teamToAgencyRepository.findByAgencyId(userDTO.getAgencyId()).get(0).getTeamid();
        //calComTeamService.addUserToTeam(createdUser.getId(), teamId);


        //return new ResponseEntity<>(createdUser, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> deleteConsultant(String userId) {
        Long calcomUserId = calcomUserToConsultantRepository.findByConsultantId(userId).getCalComUserId();
        HttpStatus responseCode = calComUserService.deleteUser(calcomUserId);
        return new ResponseEntity<>(responseCode);
    }

    @Override
    public ResponseEntity<CalcomUser> updateConsultant(String userId, UserDTO userDTO) {
        return ConsultantsApi.super.updateConsultant(userId, userDTO);
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
        return ConsultantsApi.super.getAllEventTypesOfConsultant(userId);
    }

    @Override
    public ResponseEntity<MeetingSlug> getConsultantMeetingSlug(String userId) {
        MeetingSlug meetingSlug = new MeetingSlug();
        // TODO: find associated Berater
        // TODO: match associated Berater to Calcomuser
        // TODO: get meeting link for calcom user
        switch (userId) {
            case "1":
                meetingSlug.setSlug("consultant.hamburg.1");
                break;
            case "2":
                meetingSlug.setSlug("consultant.hamburg.2");
                break;
            default:
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(meetingSlug, HttpStatus.OK);
    }
}
