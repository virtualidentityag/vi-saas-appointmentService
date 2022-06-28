package com.vi.appointmentservice.service;

import com.vi.appointmentservice.agencyservice.generated.web.AgencyControllerApi;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class AgencyService {

    private final @NonNull AgencyControllerApi agencyControllerApi;


}
