package com.vi.appointmentservice.controller;

import com.vi.appointmentservice.api.model.*;
import com.vi.appointmentservice.generated.api.controller.AgenciesApi;
import com.vi.appointmentservice.model.TeamToAgency;
import com.vi.appointmentservice.repository.TeamToAgencyRepository;
import com.vi.appointmentservice.service.CalComTeamService;
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

@RestController
@Api(tags = "agency")
@Slf4j
public class AgencyController implements AgenciesApi {

    CalComTeamService calComTeamService;
    TeamToAgencyRepository teamToAgencyRepository;

    @Autowired
    public AgencyController(CalComTeamService calComTeamService, TeamToAgencyRepository teamToAgencyRepository) {
        this.calComTeamService = calComTeamService;
        this.teamToAgencyRepository = teamToAgencyRepository;
    }

    /**
     * TEMP Admin route to associate initialMeeting team to onberAgency
     *
     * @param agencyId
     * @param requestBodyString
     * @return
     */
    @PostMapping(
            value = "appointments/agencies/{agencyId}/associateTeam",
            produces = {"application/json"},
            consumes = {"application/json"}
    )
    ResponseEntity<TeamToAgency> associateAgency(@ApiParam(value = "ID of onber agency", required = true) @PathVariable("agencyId") Long agencyId, @RequestBody String requestBodyString) {
        try {
            JSONObject requestBody = new JSONObject(requestBodyString);
            if (agencyId != null && !requestBody.isNull("calcomTeamId")) {
                TeamToAgency teamAssociation = new TeamToAgency(requestBody.getLong("calcomTeamId"), agencyId);
                return new ResponseEntity<>(teamToAgencyRepository.save(teamAssociation), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @Override
    public ResponseEntity<CalcomTeam> createAgency(AgencyResponseDTO agencyResponseDTO) {
        return AgenciesApi.super.createAgency(agencyResponseDTO);
    }

    @Override
    public ResponseEntity<Void> deleteAgency(Long agencyId) {
        return AgenciesApi.super.deleteAgency(agencyId);
    }

    @Override
    public ResponseEntity<CalcomTeam> updateAgency(Long agencyId, AgencyResponseDTO agencyResponseDTO) {
        return AgenciesApi.super.updateAgency(agencyId, agencyResponseDTO);
    }

    @Override
    public ResponseEntity<TeamEventType> addEventTypeToAgency(Long agencyId, TeamEventType teamEventType) {
        return AgenciesApi.super.addEventTypeToAgency(agencyId, teamEventType);
    }

    @Override
    public ResponseEntity<List<CalcomEventType>> getAllEventTypesOfAgency(Long agencyId) {
        return AgenciesApi.super.getAllEventTypesOfAgency(agencyId);
    }

    @Override
    public ResponseEntity<MeetingSlug> getInitialMeetingSlug(Long agencyId) {
        // TODO: add verification, sanitization and general cleanliness
        MeetingSlug meetingLink = new MeetingSlug();
        Long teamId;
        try {
            teamId = teamToAgencyRepository.findByAgencyId(agencyId).get(0).getTeamid();
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        String slug;
        try {
            slug = calComTeamService.getTeamById(teamId).getSlug();
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        meetingLink.setSlug(slug);
        return new ResponseEntity<>(meetingLink, HttpStatus.OK);
    }
}
