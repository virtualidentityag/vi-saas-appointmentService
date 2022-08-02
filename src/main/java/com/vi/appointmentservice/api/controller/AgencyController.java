package com.vi.appointmentservice.api.controller;

import com.vi.appointmentservice.api.facade.AgencyFacade;
import com.vi.appointmentservice.api.model.AgencyConsultantSyncRequestDTO;
import com.vi.appointmentservice.api.model.AgencyMasterDataSyncRequestDTO;
import com.vi.appointmentservice.api.model.AgencyResponseDTO;
import com.vi.appointmentservice.api.model.CalcomEventType;
import com.vi.appointmentservice.api.model.CalcomTeam;
import com.vi.appointmentservice.api.model.MeetingSlug;
import com.vi.appointmentservice.generated.api.controller.AgenciesApi;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.List;
import javax.validation.Valid;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Api(tags = "agency")
@Slf4j
@RequiredArgsConstructor
public class AgencyController implements AgenciesApi {

  @NonNull
  private final AgencyFacade agencyFacade;

  @Override
  public ResponseEntity<CalcomTeam> createAgency(AgencyResponseDTO agencyResponseDTO) {
    return AgenciesApi.super.createAgency(agencyResponseDTO);
  }

  @Override
  public ResponseEntity<Void> deleteAgency(Long agencyId) {
    agencyFacade.deleteAgency(agencyId);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @Override
  public ResponseEntity<CalcomTeam> updateAgency(Long agencyId,
      AgencyResponseDTO agencyResponseDTO) {
    return AgenciesApi.super.updateAgency(agencyId, agencyResponseDTO);
  }

  @Override
  public ResponseEntity<CalcomEventType> addEventTypeToAgency(Long agencyId,
      CalcomEventType teamEventType) {
    return AgenciesApi.super.addEventTypeToAgency(agencyId, teamEventType);
  }

  @Override
  public ResponseEntity<List<CalcomEventType>> getAllEventTypesOfAgency(Long agencyId) {
    List<CalcomEventType> eventTypes;
    eventTypes = this.agencyFacade.getCalcomEventTypesByAgencyId(agencyId);
    return new ResponseEntity<>(eventTypes, HttpStatus.OK);
  }

  @Override
  public ResponseEntity<MeetingSlug> getInitialMeetingSlug(Long agencyId) {
    return new ResponseEntity<>(this.agencyFacade.getMeetingSlugByAgencyId(agencyId),
        HttpStatus.OK);
  }

  @Override
  public ResponseEntity<Void> agencyConsultantsSync(
      @Valid AgencyConsultantSyncRequestDTO agencyConsultantSyncRequestDTO) {
    agencyFacade.agencyConsultantsSync(agencyConsultantSyncRequestDTO);
    return new ResponseEntity<Void>(HttpStatus.OK);
  }

  @Override
  public ResponseEntity<Void> agencyMasterDataSync(
      @Valid AgencyMasterDataSyncRequestDTO agencyMasterDataSyncRequestDTO) {
    agencyFacade.agencyMasterDataSync(agencyMasterDataSyncRequestDTO);
    return new ResponseEntity<Void>(HttpStatus.OK);
  }
}
