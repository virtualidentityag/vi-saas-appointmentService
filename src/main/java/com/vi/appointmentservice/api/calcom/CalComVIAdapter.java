package com.vi.appointmentservice.api.calcom;

import com.vi.appointmentservice.api.calcom.repository.UserRepository;
import com.vi.appointmentservice.api.model.EventTypeDTO;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CalComVIAdapter {


  public List<EventTypeDTO> getAllEventTypesOfUser(Long id) {
    throw new UnsupportedOperationException();
  }

  public void deleteAllEventTypesOfUser(Long calcomUserId) {

  }


}
