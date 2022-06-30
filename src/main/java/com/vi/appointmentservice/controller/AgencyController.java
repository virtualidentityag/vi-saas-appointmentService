package com.vi.appointmentservice.controller;

import com.vi.appointmentservice.api.model.CalcomEventType;
import com.vi.appointmentservice.api.model.MeetingLink;
import com.vi.appointmentservice.generated.api.controller.AgencyApi;
import com.vi.appointmentservice.service.CalComAgencyService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Api(tags = "agency")
@Slf4j
public class AgencyController implements AgencyApi {

    CalComAgencyService calComAgencyService;

    @Autowired
    public AgencyController(CalComAgencyService calComAgencyService) {
        this.calComAgencyService = calComAgencyService;
    }

    @Override
    public ResponseEntity<CalcomEventType> addEventTypeToAgency(Long agencyId, CalcomEventType calcomEventType) {
        return AgencyApi.super.addEventTypeToAgency(agencyId, calcomEventType);
    }

    @Override
    public ResponseEntity<List<CalcomEventType>> getAllEventTypesOfAgency(Long agencyId) {
        return AgencyApi.super.getAllEventTypesOfAgency(agencyId);
    }

    @Override
    public ResponseEntity<MeetingLink> getInitialMeetingLink(Long agencyId) {
        MeetingLink meetingLink = new MeetingLink();
        switch(agencyId.intValue()){
            case 1 :
                meetingLink.setMeetlingLink("https://calcom-develop.suchtberatung.digital/team/team-munich");
                break;
            case 2:
                meetingLink.setMeetlingLink("https://calcom-develop.suchtberatung.digital/team/team-hamburg");
                break;
            default:
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(meetingLink, HttpStatus.OK);
    }
}
