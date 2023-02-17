package com.vi.appointmentservice.api.calcom;

import com.vi.appointmentservice.api.calcom.model.CalcomTeam;
import com.vi.appointmentservice.api.calcom.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CalComVIAdapter {

  private final TeamRepository teamRepository;

  public CalcomTeam getTeamById(Long teamId){
    return teamRepository.getTeamById(teamId);
  }

}
