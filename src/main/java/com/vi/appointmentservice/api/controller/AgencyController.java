package com.vi.appointmentservice.api.controller;

import com.vi.appointmentservice.api.calcom.model.EventType;
import com.vi.appointmentservice.api.facade.AgencyFacade;
import com.vi.appointmentservice.api.model.AgencyConsultantSyncRequestDTO;
import com.vi.appointmentservice.api.model.AgencyMasterDataSyncRequestDTO;
import com.vi.appointmentservice.api.model.CalcomEventTypeDTO;
import com.vi.appointmentservice.api.model.CreateUpdateCalcomEventTypeDTO;
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
    return new ResponseEntity<Void>(HttpStatus.OK);
  }

  @Override
  public ResponseEntity<List<CalcomEventTypeDTO>> getAllEventTypesOfAgency(Long agencyId) {
    List<CalcomEventTypeDTO> eventTypes = this.agencyFacade.getAgencyEventTypes(agencyId).stream()
        .map(el -> asEventTypeDTO(el)).collect(
            Collectors.toList());
    return new ResponseEntity<>(eventTypes, HttpStatus.OK);
  }

  @Override
  public ResponseEntity<CalcomEventTypeDTO> getAgencyEventTypeById(Long agencyId,
      Long eventTypeId) {
    //TODO: agencyId is not needed. rename this method. needs to be in sync with frontend
    return new ResponseEntity<>(
        asEventTypeDTO(this.agencyFacade.getAgencyEventTypeById(eventTypeId)),
        HttpStatus.OK);
  }

  @Override
  public ResponseEntity<CalcomEventTypeDTO> addEventTypeToAgency(Long agencyId,
      CreateUpdateCalcomEventTypeDTO teamEventType) {
    return new ResponseEntity<>(
        asEventTypeDTO(this.agencyFacade.createAgencyEventType(agencyId, teamEventType)),
        HttpStatus.OK);
  }

  @Override
  public ResponseEntity<CalcomEventTypeDTO> updateAgencyEventType(Long agencyId, Long eventTypeId,
      CreateUpdateCalcomEventTypeDTO createUpdateCalcomEventTypeDTO) {
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
    return new ResponseEntity<Void>(HttpStatus.OK);
  }

  private CalcomEventTypeDTO asEventTypeDTO(EventType eventType) {
    CalcomEventTypeDTO calcomEventType = new CalcomEventTypeDTO();
    calcomEventType.setTitle(eventType.getTitle());
    calcomEventType.setId(eventType.getId());
    calcomEventType.setLength(eventType.getLength());
    calcomEventType.setDescription(eventType.getDescription());
    calcomEventType.setConsultants(eventType.getConsultants());
    return calcomEventType;
  }

  @Override
  public ResponseEntity<Void> deleteAgency(Long agencyId) {
    agencyFacade.deleteAgency(agencyId);
    return new ResponseEntity<>(HttpStatus.OK);
  }

}
