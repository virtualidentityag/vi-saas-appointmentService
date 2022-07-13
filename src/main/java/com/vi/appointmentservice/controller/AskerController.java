package com.vi.appointmentservice.controller;

import com.vi.appointmentservice.api.model.CalcomBooking;
import com.vi.appointmentservice.api.model.CalcomWebhook;
import com.vi.appointmentservice.api.model.CalcomWebhookPayload;
import com.vi.appointmentservice.api.model.MeetingSlug;
import com.vi.appointmentservice.generated.api.controller.AskersApi;
import com.vi.appointmentservice.model.CalcomBookingToAsker;
import com.vi.appointmentservice.repository.CalcomBookingToAskerRepository;
import com.vi.appointmentservice.service.CalComBookingService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;


import java.util.ArrayList;
import java.util.List;


@RestController
@Api(tags = "asker")
@Slf4j
public class AskerController implements AskersApi {

    CalComBookingService calComBookingService;



    CalcomBookingToAskerRepository calcomBookingToAskerRepository;

    @Autowired
    public AskerController(
            CalComBookingService calComBookingService,
            CalcomBookingToAskerRepository calcomBookingToAskerRepository)
    {
        this.calComBookingService = calComBookingService;
        this.calcomBookingToAskerRepository = calcomBookingToAskerRepository;
    }


    @Override
    public ResponseEntity<List<CalcomBooking>> getAllBookingsOfAsker(String askerId) {
        try {
            List<CalcomBookingToAsker> bookingIds = calcomBookingToAskerRepository.findByAskerId(askerId);
            List<CalcomBooking> bookings = new ArrayList<>();

            for (CalcomBookingToAsker bookingId : bookingIds) {
                bookings.add(calComBookingService.getBookingById(bookingId.getCalcomBookingId()));
            }

            return new ResponseEntity<>(bookings, HttpStatus.OK);
        }
        catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<CalcomBooking> getBookingDetails(String bookingId) {
        try {
            CalcomBooking booking = calComBookingService.getBookingById(Long.valueOf(bookingId));
            return new ResponseEntity<>(booking, HttpStatus.OK);
        }
        catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<MeetingSlug> getAskerMeetingSlug(String askerId) {
        return AskersApi.super.getAskerMeetingSlug(askerId);
    }

    @Override
    public ResponseEntity<String> processBooking(CalcomWebhook calcomWebhook) {
        try {
            CalcomWebhookPayload payload = calcomWebhook.getPayload();

            if (payload != null) {
                String askerId = payload.getMetadata().getUser();
                Long bookingId = Long.valueOf(payload.getBookingId());

                CalcomBookingToAsker userAssociation = new CalcomBookingToAsker(bookingId, askerId);

                if (calcomWebhook.getTriggerEvent().equals("BOOKING_CREATED")) {
                    calcomBookingToAskerRepository.save(userAssociation);

                } else {
                    calcomBookingToAskerRepository.delete(userAssociation);
                }

                return new ResponseEntity<>(String.valueOf(bookingId), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
