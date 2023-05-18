package com.vi.appointmentservice.api.controller;

import com.vi.appointmentservice.api.calcom.model.CalcomEventType;
import com.vi.appointmentservice.api.calcom.service.EventTypeMapper;
import com.vi.appointmentservice.api.facade.AgencyFacade;
import com.vi.appointmentservice.api.model.AgencyConsultantSyncRequestDTO;
import com.vi.appointmentservice.api.model.AgencyMasterDataSyncRequestDTO;
import com.vi.appointmentservice.api.model.CreateUpdateEventTypeDTO;
import com.vi.appointmentservice.api.model.EventTypeDTO;
import com.vi.appointmentservice.api.model.MeetingSlug;
import com.vi.appointmentservice.api.model.TeamEventTypeConsultant;
import com.vi.appointmentservice.generated.api.controller.AgenciesApi;
import io.swagger.annotations.Api;
import java.util.List;
import java.util.stream.Collectors;
import javax.validation.Valid;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Api(tags = "agency")
@Slf4j
@RequiredArgsConstructor
public class AgencyController implements AgenciesApi {

  @NonNull
  private final AgencyFacade agencyFacade;

  @Override
  public ResponseEntity<Void> agencyMasterDataSync(
      @Valid AgencyMasterDataSyncRequestDTO agencyMasterDataSyncRequestDTO) {
    agencyFacade.agencyMasterDataSync(agencyMasterDataSyncRequestDTO);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @Override
  public ResponseEntity<List<EventTypeDTO>> getAllEventTypesOfAgency(Long agencyId) {
    List<EventTypeDTO> eventTypes = this.agencyFacade.getAgencyEventTypes(agencyId).stream()
        .map(this::asEventTypeDTO).collect(
            Collectors.toList());
    return new ResponseEntity<>(eventTypes, HttpStatus.OK);
  }

  @Override
  public ResponseEntity<EventTypeDTO> getAgencyEventTypeById(Long agencyId,
      Long eventTypeId) {
    return new ResponseEntity<>(
        asEventTypeDTO(this.agencyFacade.getAgencyEventTypeById(eventTypeId)),
        HttpStatus.OK);
  }

  @Override
  public ResponseEntity<EventTypeDTO> addEventTypeToAgency(Long agencyId,
      CreateUpdateEventTypeDTO teamEventType) {
    return new ResponseEntity<>(
        asEventTypeDTO(this.agencyFacade.createAgencyEventType(agencyId, teamEventType)),
        HttpStatus.OK);
  }

  @Override
  public ResponseEntity<EventTypeDTO> updateAgencyEventType(Long agencyId, Long eventTypeId,
      CreateUpdateEventTypeDTO createUpdateCalcomEventTypeDTO) {
    return new ResponseEntity<>(
        asEventTypeDTO(
            this.agencyFacade.updateAgencyEventType(eventTypeId, createUpdateCalcomEventTypeDTO)),
        HttpStatus.OK);
  }

  @Override
  public ResponseEntity<Void> deleteAgencyEventType(Long agencyId, Long eventTypeId) {
    this.agencyFacade.deleteAgencyEventType(eventTypeId);
    return new ResponseEntity<>(HttpStatus.OK);
  }

  @Override
  public ResponseEntity<List<TeamEventTypeConsultant>> getAllConsultantsOfAgency(Long agencyId) {
    List<TeamEventTypeConsultant> availableConsultants;
    availableConsultants = this.agencyFacade.getAllConsultantsOfAgency(agencyId);
    return new ResponseEntity<>(availableConsultants, HttpStatus.OK);
  }

  @Override
  public ResponseEntity<MeetingSlug> getInitialMeetingSlug(Long agencyId) {
    return new ResponseEntity<>(this.agencyFacade.getMeetingSlugByAgencyId(agencyId),
        HttpStatus.OK);
  }

  @Override
  public ResponseEntity<Void> agencyConsultantsSync(
      @Valid AgencyConsultantSyncRequestDTO req) {
    agencyFacade.assignConsultant2AppointmentTeams(req.getConsultantId(), req.getAgencies());
    return new ResponseEntity<>(HttpStatus.OK);
  }

  private EventTypeDTO asEventTypeDTO(CalcomEventType eventType) {
    return new EventTypeMapper().asEventTypeDTO(eventType);
  }

  @Override
  public ResponseEntity<Void> deleteAgency(Long agencyId) {
    agencyFacade.deleteAgency(agencyId);
    return new ResponseEntity<>(HttpStatus.OK);
  }

}
