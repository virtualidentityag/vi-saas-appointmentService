package com.vi.appointmentservice.service;

import com.vi.appointmentservice.userservice.generated.web.UserControllerApi;
import com.vi.appointmentservice.userservice.generated.web.model.AgencyDTO;
import com.vi.appointmentservice.userservice.generated.web.model.UserDataResponseDTO;
import com.vi.appointmentservice.userservice.generated.web.model.ConsultantDTO;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {
    private final @NonNull UserControllerApi userControllerApi;

    public List<AgencyDTO> getAgenciesOfUser() {
        UserDataResponseDTO user = userControllerApi.getUserData();
        return user.getAgencies();
    }

    public ConsultantDTO getConsultantOfUser(String userId) {
        // TODO: Get and return agencyID somehow
        return new ConsultantDTO();
    }
}
