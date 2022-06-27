package com.vi.appointmentservice.controller;

import com.vi.appointmentservice.api.model.EventType;
import com.vi.appointmentservice.api.model.Team;
import com.vi.appointmentservice.generated.api.controller.TeamApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controller for team API operations.
 */
@RestController
@RequiredArgsConstructor
@Slf4j
public class TeamController implements TeamApi {
    @Override
    public ResponseEntity<Void> addEventTypeToTeam(Long teamId, EventType body) {
        return TeamApi.super.addEventTypeToTeam(teamId, body);
    }

    @Override
    public ResponseEntity<Void> addTeam(Team body) {
        return TeamApi.super.addTeam(body);
    }

    @Override
    public ResponseEntity<Void> deleteTeam(Long teamId) {
        return TeamApi.super.deleteTeam(teamId);
    }

    @Override
    public ResponseEntity<List<EventType>> getAllEventTypesOfTeam(Long teamId) {
        return TeamApi.super.getAllEventTypesOfTeam(teamId);
    }

    @Override
    public ResponseEntity<List<Team>> getAllTeams() {
        return TeamApi.super.getAllTeams();
    }

    @Override
    public ResponseEntity<Team> getTeamById(Long teamId) {
        return TeamApi.super.getTeamById(teamId);
    }

    @Override
    public ResponseEntity<Void> updateTeam(Team body) {
        return TeamApi.super.updateTeam(body);
    }

    @Override
    public ResponseEntity<Void> updateTeamWithForm(Long teamId) {
        return TeamApi.super.updateTeamWithForm(teamId);
    }
}
