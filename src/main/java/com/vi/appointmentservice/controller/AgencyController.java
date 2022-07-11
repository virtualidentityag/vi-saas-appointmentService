package com.vi.appointmentservice.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.vi.appointmentservice.api.model.*;
import com.vi.appointmentservice.generated.api.controller.AgenciesApi;
import com.vi.appointmentservice.model.TeamToAgency;
import com.vi.appointmentservice.repository.TeamToAgencyRepository;
import com.vi.appointmentservice.service.CalComEventTypeService;
import com.vi.appointmentservice.service.CalComTeamService;
import io.swagger.annotations.*;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Api(tags = "agency")
@Slf4j
@RequiredArgsConstructor
public class AgencyController implements AgenciesApi {

    @NonNull private final CalComTeamService calComTeamService;

    @NonNull private final CalComEventTypeService calComEventTypeService;
    @NonNull private final TeamToAgencyRepository teamToAgencyRepository;


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
    public ResponseEntity<CalcomEventType> addEventTypeToAgency(Long agencyId, CalcomEventType teamEventType) {
        Long teamId = teamToAgencyRepository.findByAgencyId(agencyId).get(0).getTeamid();
        // TODO: Get User IDs
        // TODO: save eventType
        // TODO: EVENT TYPE USER API??
        return AgenciesApi.super.addEventTypeToAgency(agencyId, teamEventType);
    }

    @Override
    public ResponseEntity<List<CalcomEventType>> getAllEventTypesOfAgency(Long agencyId) {
        // TODO: remove "true" once team are associated to agencies
        if(true || teamToAgencyRepository.existsByAgencyId(agencyId)){
            List<CalcomEventType> eventTypes;
            try {
                // eventTypes = calComEventTypeService.getAllEventTypesOfTeam(teamToAgencyRepository.findByAgencyId(agencyId).get(0).getTeamid());
                eventTypes = calComEventTypeService.getAllEventTypesOfTeam(0L);
            } catch (JsonProcessingException e) {
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
            return new ResponseEntity<>(eventTypes, HttpStatus.OK);
        }else{
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @Override
    public ResponseEntity<MeetingSlug> getInitialMeetingSlug(Long agencyId) {
        // TODO: add verification, sanitization and general cleanliness
        MeetingSlug meetingSlug = new MeetingSlug();
        switch (agencyId.intValue()) {
            case 1:
                meetingSlug.setSlug("team-munich");
                break;
            case 2:
                meetingSlug.setSlug("team-hamburg");
                break;
            default:
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(meetingSlug, HttpStatus.OK);
    }

    @ApiOperation(value = "Get initial meeting booking link for agency", nickname = "getInitialMeetingSlugReal", notes = "", response = MeetingSlug.class, tags={ "agency", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "successful operation", response = MeetingSlug.class) })
    @GetMapping(
            value = "/agencies/{agencyId}/initialMeetingSlugReal",
            produces = { "application/json" }
    )
    public ResponseEntity<MeetingSlug> getInitialMeetingSlugReal(@ApiParam(value = "ID of agency",required=true) @PathVariable("agencyId") Long agencyId) {
        // TODO: add verification, sanitization and general cleanliness
        MeetingSlug meetingSlug = new MeetingSlug();
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
        meetingSlug.setSlug(slug);
        return new ResponseEntity<>(meetingSlug, HttpStatus.OK);
    }
}
