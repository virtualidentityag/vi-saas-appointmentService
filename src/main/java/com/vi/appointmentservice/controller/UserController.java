package com.vi.appointmentservice.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vi.appointmentservice.api.model.*;
import com.vi.appointmentservice.generated.api.controller.UserApi;
import com.vi.appointmentservice.service.CalComUserService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @Autowired
    public UserController(CalComUserService calComUserService, ObjectMapper objectMapper) {
        this.calComUserService = calComUserService;
    }

    @Override
    public ResponseEntity<CalcomEventType> addEventTypeToUser(String userId, CalcomEventType calcomEventType) {
        return UserApi.super.addEventTypeToUser(userId, calcomEventType);
    }

    @Override
    public ResponseEntity<CalcomUser> createUser(OnberUser onberUser) {
        return UserApi.super.createUser(onberUser);
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
    public ResponseEntity<String> getUserMeetingLink(String userId) {
        switch(userId){
            case "1":
                return new ResponseEntity<>("https://calcom-develop.suchtberatung.digital/consultant.hamburg.1", HttpStatus.OK);
            case "2":
                return new ResponseEntity<>("https://calcom-develop.suchtberatung.digital/consultant.hamburg.2", HttpStatus.OK);
            default:
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}
