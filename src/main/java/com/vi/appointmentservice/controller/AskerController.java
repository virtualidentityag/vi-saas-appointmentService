package com.vi.appointmentservice.controller;

import com.vi.appointmentservice.api.model.CalcomBooking;
import com.vi.appointmentservice.api.model.MeetingSlug;
import com.vi.appointmentservice.generated.api.controller.AskersApi;
import org.springframework.http.ResponseEntity;

import java.util.List;

public class AskerController implements AskersApi {

    @Override
    public ResponseEntity<List<CalcomBooking>> getAllBookingsOfAsker(String askerId) {
        return AskersApi.super.getAllBookingsOfAsker(askerId);
    }

    @Override
    public ResponseEntity<MeetingSlug> getAskerMeetingSlug(String askerId) {
        return AskersApi.super.getAskerMeetingSlug(askerId);
    }
}
