package com.vi.appointmentservice.controller;

import com.vi.appointmentservice.api.model.*;
import com.vi.appointmentservice.generated.api.controller.UserApi;
import com.vi.appointmentservice.model.CalcomUserToUser;
import com.vi.appointmentservice.repository.CalcomUserToUserRepository;
import com.vi.appointmentservice.repository.TeamToAgencyRepository;
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

/**
 * Controller for user API operations.
 */
@RestController
@Api(tags = "user")
@Slf4j
public class UserController implements UserApi {

    CalComUserService calComUserService;
    CalComTeamService calComTeamService;
    CalcomUserToUserRepository calcomUserToUserRepository;
    TeamToAgencyRepository teamToAgencyRepository;


    @Autowired
    public UserController(CalComUserService calComUserService, CalComTeamService calComTeamService, CalcomUserToUserRepository calcomUserToUserRepository, TeamToAgencyRepository teamToAgencyRepository) {
        this.calComUserService = calComUserService;
        this.calComTeamService = calComTeamService;
        this.calcomUserToUserRepository = calcomUserToUserRepository;
        this.teamToAgencyRepository = teamToAgencyRepository;
    }

    /**
     * TEMP Admin route to associate onberUser to calcomUser
     *
     * @param userId
     * @param requestBodyString
     * @return
     */
    @PostMapping(
            value = "/user/{userId}/associateUser",
            produces = {"application/json"},
            consumes = {"application/json"}
    )
    ResponseEntity<CalcomUserToUser> associateUser(@ApiParam(value = "ID of onber user", required = true) @PathVariable("userId") String userId, @RequestBody String requestBodyString) {
        try {
            JSONObject requestBody = new JSONObject(requestBodyString);
            if (userId != null && !userId.isEmpty() && !requestBody.isNull("calcomUserId")) {
                CalcomUserToUser userAssociation = new CalcomUserToUser(userId, requestBody.getLong("calcomUserId"));
                return new ResponseEntity<>(calcomUserToUserRepository.save(userAssociation), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<CalcomUser> createUser(UserDTO userDTO) {
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
    public ResponseEntity<Void> deleteUser(String userId) {
        Long calcomUserId = calcomUserToUserRepository.findByUserId(userId).getCalComUserId();
        HttpStatus responseCode = calComUserService.deleteUser(calcomUserId);
        return new ResponseEntity<>(responseCode);
    }

    @Override
    public ResponseEntity<CalcomUser> updateUser(String userId, UserDTO userDTO) {
        return UserApi.super.updateUser(userId, userDTO);
    }

    @Override
    public ResponseEntity<CalcomEventType> addEventTypeToUser(String userId, CalcomEventType calcomEventType) {
        return UserApi.super.addEventTypeToUser(userId, calcomEventType);
    }


    @Override
    public ResponseEntity<List<CalcomBooking>> getAllBookingsOfUser(String userId) {
        return UserApi.super.getAllBookingsOfUser(userId);
    }

    @Override
    public ResponseEntity<List<CalcomEventType>> getAllEventTypesOfUser(String userId) {
        return UserApi.super.getAllEventTypesOfUser(userId);
    }

    @Override
    public ResponseEntity<MeetingLink> getUserMeetingLink(String userId) {
        MeetingLink meetingLink = new MeetingLink();
        // TODO: find associated Berater
        // TODO: match associated Berater to Calcomuser
        // TODO: get meeting link for calcom user
        switch (userId) {
            case "1":
                meetingLink.setMeetlingLink("https://calcom-develop.suchtberatung.digital/consultant.hamburg.1");
                break;
            case "2":
                meetingLink.setMeetlingLink("https://calcom-develop.suchtberatung.digital/consultant.hamburg.2");
                break;
            default:
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(meetingLink, HttpStatus.OK);
    }
}
