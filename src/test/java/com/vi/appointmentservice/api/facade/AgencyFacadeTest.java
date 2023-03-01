package com.vi.appointmentservice.api.facade;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.vi.appointmentservice.api.calcom.model.CalcomTeam;
import com.vi.appointmentservice.api.calcom.service.CalComTeamService;
import com.vi.appointmentservice.api.calcom.service.CalComUserService;
import com.vi.appointmentservice.api.calcom.service.CalcomEventTypeService;
import com.vi.appointmentservice.api.model.AgencyMasterDataSyncRequestDTO;
import com.vi.appointmentservice.api.service.AppointmentService;
import com.vi.appointmentservice.model.CalcomUserToConsultant;
import com.vi.appointmentservice.model.TeamToAgency;
import com.vi.appointmentservice.repository.TeamToAgencyRepository;
import com.vi.appointmentservice.repository.UserToConsultantRepository;
import java.util.Optional;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AgencyFacadeTest {

  public static final long CALCOM_TEAM_ID = 2L;
  public static final long AGENCY_ID = 1L;
  private static final String CONSULTANT_ID = "consultantId";
  private static final Long CALCOM_USER_ID = 5L;
  @InjectMocks
  AgencyFacade agencyFacade;

  @Mock
  TeamToAgencyRepository teamToAgencyRepository;

  @Mock
  UserToConsultantRepository user2ConsultantRepo;

  @Mock
  CalComUserService calComUserService;

  @Mock
  CalComTeamService calComTeamService;

  @Mock
  CalcomEventTypeService calcomEventTypeService;

  @Mock
  AppointmentService appointmentService;


  @Test
  void agencyMasterDataSync_Should_updateAppointmentTeam_When_AppointmentTeamExists() {
    // given
    when(teamToAgencyRepository.existsByAgencyId(AGENCY_ID)).thenReturn(true);
    when(teamToAgencyRepository.findByAgencyId(AGENCY_ID)).thenReturn(Optional.of(giveTeamToAgency()));

    // when
    agencyFacade.agencyMasterDataSync(new AgencyMasterDataSyncRequestDTO().id(AGENCY_ID).name("agencyName"));

    // then
    verify(calComTeamService).updateTeam(CALCOM_TEAM_ID, "agencyName");
  }

  private  TeamToAgency giveTeamToAgency() {
    TeamToAgency teamToAgency = new TeamToAgency();
    teamToAgency.setTeamid(CALCOM_TEAM_ID);
    return teamToAgency;
  }

  @Test
  void agencyMasterDataSync_Should_createAgencyToCalcomTeamRelation_When_AppointmentTeamDoesNotExists() {
    // given

    when(teamToAgencyRepository.existsByAgencyId(AGENCY_ID)).thenReturn(false);
    AppointmentType appointmentType = new AppointmentType();
    when(appointmentService.createDefaultAppointmentType()).thenReturn(appointmentType);
    CalcomTeam calcomTeam = new CalcomTeam();
    calcomTeam.setId(CALCOM_TEAM_ID);
    when(calComTeamService.createTeam(Mockito.anyString())).thenReturn(calcomTeam);

    // when
    agencyFacade.agencyMasterDataSync(new AgencyMasterDataSyncRequestDTO().id(AGENCY_ID).name("agencyName"));

    // then
    verify(appointmentService).createDefaultAppointmentType();
    verify(calComTeamService).createTeam("agencyName");
    verify(calcomEventTypeService).createEventType(CALCOM_TEAM_ID, appointmentType);
    ArgumentCaptor<TeamToAgency> argumentCaptor = ArgumentCaptor.forClass(TeamToAgency.class);
    verify(teamToAgencyRepository).save(argumentCaptor.capture());

    TeamToAgency value = argumentCaptor.getValue();
    assertThat(value.getAgencyId()).isEqualTo(AGENCY_ID);
    assertThat(value.getTeamid()).isEqualTo(CALCOM_TEAM_ID);
  }

  @Test
  void assignConsultant2AppointmentTeams_Should_ClearPreviousUserMembershipsAndAddUser2Team() {
    // given
    CalcomUserToConsultant calcomUser = giveCalcomUserToConsultant();
    when(user2ConsultantRepo.findByConsultantId(CONSULTANT_ID)).thenReturn(Optional.of(calcomUser));
    when(teamToAgencyRepository.findByAgencyId(AGENCY_ID)).thenReturn(Optional.of(giveTeamToAgency()));

    // when
    agencyFacade.assignConsultant2AppointmentTeams(CONSULTANT_ID, Lists.newArrayList(AGENCY_ID));

    // then
    verify(user2ConsultantRepo).findByConsultantId(CONSULTANT_ID);
    verify(calcomEventTypeService).cleanUserMemberships(CALCOM_USER_ID, Lists.newArrayList(CALCOM_TEAM_ID));
    verify(calcomEventTypeService).addUser2Team(CALCOM_USER_ID, CALCOM_TEAM_ID);
  }

  private static CalcomUserToConsultant giveCalcomUserToConsultant() {
    CalcomUserToConsultant calcomUser = new CalcomUserToConsultant();
    calcomUser.setConsultantId(CONSULTANT_ID);
    calcomUser.setCalComUserId(CALCOM_USER_ID);
    return calcomUser;
  }
}