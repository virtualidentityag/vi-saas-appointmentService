package com.vi.appointmentservice.api.service.onlineberatung;

import com.vi.appointmentservice.agencyservice.generated.web.AgencyControllerApi;
import com.vi.appointmentservice.api.service.securityheader.SecurityHeaderSupplier;
import com.vi.appointmentservice.agencyservice.generated.ApiClient;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
//TODO: do we use this at all?
public class AgencyService {

    private final @NonNull AgencyControllerApi agencyControllerApi;
    private final @NonNull SecurityHeaderSupplier securityHeaderSupplier;

    private void addDefaultHeaders(ApiClient apiClient) {
        var headers = this.securityHeaderSupplier.getKeycloakAndCsrfHttpHeaders();
        headers.forEach((key, value) -> apiClient.addDefaultHeader(key, value.iterator().next()));
    }

}
