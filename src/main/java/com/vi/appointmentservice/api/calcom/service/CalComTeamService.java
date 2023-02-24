package com.vi.appointmentservice.api.calcom.service;

import com.vi.appointmentservice.api.calcom.model.CalcomTeam;
import com.vi.appointmentservice.api.calcom.repository.TeamRepository;
import com.vi.appointmentservice.api.calcom.repository.MembershipsRepository;
import java.util.List;
import java.util.UUID;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CalComTeamService {

  private final TeamRepository teamRepository;

  @NonNull
  private final MembershipsRepository membershipsRepository;

  public CalcomTeam getTeamById(Number teamId) {
    return teamRepository.getTeamById(teamId);
  }

  public CalcomTeam createTeam(String teamName) {
    CalcomTeam team = new CalcomTeam();
    team.setName(teamName);
    team.setSlug(UUID.randomUUID().toString());
    team = teamRepository.createTeam(team);
    return team;
  }

  public CalcomTeam updateTeam(Long teamId, String teamName) {
    CalcomTeam teamById = teamRepository.getTeamById(teamId);
    teamById.setName(teamName);
    return teamRepository.updateTeam(teamById);
  }

  public void deleteTeam(Long teamId) {
    membershipsRepository.deleteTeamMemeberships(teamId);
    teamRepository.deleteTeam(teamId);
  }

  public List<Long> getTeamMembers(Long teamId) {
    return membershipsRepository.getUsersOfTeam(teamId);
  }

}
