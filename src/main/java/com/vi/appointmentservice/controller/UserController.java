package com.vi.appointmentservice.controller;

import com.vi.appointmentservice.api.model.Availability;
import com.vi.appointmentservice.api.model.Booking;
import com.vi.appointmentservice.api.model.EventType;
import com.vi.appointmentservice.api.model.User;
import com.vi.appointmentservice.generated.api.controller.UserApi;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controller for user API operations.
 */
@RestController
@RequiredArgsConstructor
@Api(tags = "user")
@Slf4j
public class UserController implements UserApi {

    @Override
    public ResponseEntity<Void> addEventTypeToUser(Long userId, EventType body) {
        return UserApi.super.addEventTypeToUser(userId, body);
    }

    @Override
    public ResponseEntity<Void> createUser(User body) {
        return UserApi.super.createUser(body);
    }

    @Override
    public ResponseEntity<Void> deleteUser(Long userId) {
        return UserApi.super.deleteUser(userId);
    }

    @Override
    public ResponseEntity<List<Availability>> getAllAvailabilitiesOfUser(Long userId) {
        return UserApi.super.getAllAvailabilitiesOfUser(userId);
    }

    @Override
    public ResponseEntity<List<Booking>> getAllBookingsOfUser(Long userId) {
        return UserApi.super.getAllBookingsOfUser(userId);
    }

    @Override
    public ResponseEntity<List<EventType>> getAllEventTypesOfUser(Long userId) {
        return UserApi.super.getAllEventTypesOfUser(userId);
    }

    @Override
    public ResponseEntity<User> getUserById(Long userId) {
        return new ResponseEntity("TEST!!", HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> updateUser(User body) {
        return UserApi.super.updateUser(body);
    }

    @Override
    public ResponseEntity<Void> updateUserWithForm(Long userId, String name, String status) {
        return UserApi.super.updateUserWithForm(userId, name, status);
    }
}
