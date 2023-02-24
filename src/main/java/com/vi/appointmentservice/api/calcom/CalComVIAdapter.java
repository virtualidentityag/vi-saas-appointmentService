package com.vi.appointmentservice.api.calcom;

import com.vi.appointmentservice.api.calcom.model.CalcomUser;
import com.vi.appointmentservice.api.model.CalcomEventTypeDTO;
import com.vi.appointmentservice.api.calcom.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CalComVIAdapter {

  private final UserRepository userRepository;



  public CalcomEventTypeDTO getEventTypeById(Long eventTypeId) {
    throw new UnsupportedOperationException();
  }

  public List<CalcomEventTypeDTO> getAllEventTypesOfTeam(Long teamId) {
    throw new UnsupportedOperationException();
  }

  public List<CalcomEventTypeDTO> getAllEventTypesOfUser(Long id) {
    throw new UnsupportedOperationException();
  }

  public void deleteAllEventTypesOfUser(Long calcomUserId) {

  }


}
