package com.vi.appointmentservice.api.facade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.vi.appointmentservice.api.calcom.model.CalcomEventType;
import com.vi.appointmentservice.api.calcom.model.CalcomTeam;
import com.vi.appointmentservice.api.calcom.model.CalcomUser;
import com.vi.appointmentservice.api.calcom.service.CalComTeamService;
import com.vi.appointmentservice.api.calcom.service.CalComUserService;
import com.vi.appointmentservice.api.calcom.service.CalcomEventTypeService;
import com.vi.appointmentservice.api.model.AgencyMasterDataSyncRequestDTO;
import com.vi.appointmentservice.api.model.CreateUpdateEventTypeDTO;
import com.vi.appointmentservice.api.model.MeetingSlug;
import com.vi.appointmentservice.api.model.TeamEventTypeConsultant;
import com.vi.appointmentservice.api.service.AppointmentService;
import com.vi.appointmentservice.model.CalcomUserToConsultant;
import com.vi.appointmentservice.model.TeamToAgency;
import com.vi.appointmentservice.repository.TeamToAgencyRepository;
import com.vi.appointmentservice.repository.UserToConsultantRepository;
import java.util.List;
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
  private static final String CALCOM_TEAM_SLUG = "slug";
  public static final long EVENT_TYPE_ID = 3L;
  public static final long CALCOM_ADMIN_USER_ID = 1L;
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
    CalcomTeam calcomTeam = giveCalcomTeam();
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

  private static CalcomTeam giveCalcomTeam() {
    CalcomTeam calcomTeam = new CalcomTeam();
    calcomTeam.setId(CALCOM_TEAM_ID);
    calcomTeam.setSlug(CALCOM_TEAM_SLUG);
    return calcomTeam;
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

  @Test
  void getAllConsultantsOfAgency_Should_GetAllConsultantsAndExcludeCalcomAdminUser() {
    // given
    when(teamToAgencyRepository.findByAgencyId(AGENCY_ID)).thenReturn(Optional.of(giveTeamToAgency()));
    when(calComTeamService.getTeamMembers(CALCOM_TEAM_ID)).thenReturn(Lists.newArrayList(CALCOM_USER_ID,
        CALCOM_ADMIN_USER_ID));
    CalcomUser calcomUser = new CalcomUser();
    calcomUser.setName("Calcom user name");
    when(calComUserService.getUserById(CALCOM_USER_ID)).thenReturn(calcomUser);
    when(calComUserService.getUserById(CALCOM_ADMIN_USER_ID)).thenReturn(new CalcomUser());
    when(user2ConsultantRepo.findByCalComUserId(CALCOM_USER_ID)).thenReturn(Optional.of(giveCalcomUserToConsultant()));
    when(user2ConsultantRepo.findByCalComUserId(CALCOM_ADMIN_USER_ID)).thenReturn(Optional.empty());

    // when
    List<TeamEventTypeConsultant> allConsultantsOfAgency = agencyFacade.getAllConsultantsOfAgency(
        AGENCY_ID);

    // then
    assertThat(allConsultantsOfAgency).hasSize(1);
    assertThat(allConsultantsOfAgency.get(0).getConsultantId()).isEqualTo(CONSULTANT_ID);
    assertThat(allConsultantsOfAgency.get(0).getConsultantName()).isEqualTo("Calcom user name");
  }

  @Test
  void getAllConsultantsOfAgency_Should_GetAllConsultantsButSkipThoseThatAreMissingInUsers2ConsultantRepo() {
    // given
    when(teamToAgencyRepository.findByAgencyId(AGENCY_ID)).thenReturn(Optional.of(giveTeamToAgency()));
    when(calComTeamService.getTeamMembers(CALCOM_TEAM_ID)).thenReturn(Lists.newArrayList(CALCOM_USER_ID));
    CalcomUser calcomUser = new CalcomUser();
    calcomUser.setName("Calcom user name");
    when(calComUserService.getUserById(CALCOM_USER_ID)).thenReturn(calcomUser);
    when(user2ConsultantRepo.findByCalComUserId(CALCOM_USER_ID)).thenReturn(Optional.empty());

    // when
    List<TeamEventTypeConsultant> allConsultantsOfAgency = agencyFacade.getAllConsultantsOfAgency(
        AGENCY_ID);

    // then
    assertThat(allConsultantsOfAgency).isEmpty();
  }

  @Test
  void getMeetingSlugByAgencyId_Should_GetMeetingSlug() {
    // given
    when(teamToAgencyRepository.findByAgencyId(AGENCY_ID)).thenReturn(Optional.of(giveTeamToAgency()));
    when(calComTeamService.getTeamById(CALCOM_TEAM_ID)).thenReturn(giveCalcomTeam());
    // when
    MeetingSlug meetingSlugByAgencyId = agencyFacade.getMeetingSlugByAgencyId(AGENCY_ID);
    // then
    assertThat(meetingSlugByAgencyId.getSlug()).isEqualTo(CALCOM_TEAM_SLUG);
  }

  @Test
  void createAgencyEventType_Should_createAgencyEventType() {
    // given
    when(teamToAgencyRepository.findByAgencyId(AGENCY_ID)).thenReturn(Optional.of(giveTeamToAgency()));
    when(calComTeamService.getTeamById(null)).thenReturn(giveCalcomTeam());
    AppointmentType appointmentType = new AppointmentType();
    when(appointmentService.createDefaultAppointmentType()).thenReturn(appointmentType);
    when(calcomEventTypeService.createEventType(CALCOM_TEAM_ID, new AppointmentType())).thenReturn(new CalcomEventType());
    when(user2ConsultantRepo.findByConsultantId(CONSULTANT_ID)).thenReturn(Optional.of(giveCalcomUserToConsultant()));
    when(calComUserService.getUserById(CALCOM_USER_ID)).thenReturn(new CalcomUser());
    when(user2ConsultantRepo.findByCalComUserId(CALCOM_USER_ID)).thenReturn(Optional.of(giveCalcomUserToConsultant()));
    // when
    agencyFacade.createAgencyEventType(AGENCY_ID, new CreateUpdateEventTypeDTO().consultants(Lists.newArrayList(new TeamEventTypeConsultant().consultantId(CONSULTANT_ID))));
    // then
    verify(appointmentService).createDefaultAppointmentType();
    verify(calcomEventTypeService).createEventType(CALCOM_TEAM_ID, appointmentType);
  }


  @Test
  void updateAgencyEventType_Should_callUpdateEventTypePassingConsultantCalcomUserIdAsMember() {
    // given
    when(calcomEventTypeService.getEventTypeById(EVENT_TYPE_ID)).thenReturn(new CalcomEventType());
    when(user2ConsultantRepo.findByConsultantId(CONSULTANT_ID)).thenReturn(Optional.of(giveCalcomUserToConsultant()));

    // when
    CreateUpdateEventTypeDTO consultants = new CreateUpdateEventTypeDTO().consultants(
        Lists.newArrayList(new TeamEventTypeConsultant().consultantId(CONSULTANT_ID))).locations(Lists.newArrayList("location"));
    agencyFacade.updateAgencyEventType(EVENT_TYPE_ID, consultants);
    // then
    ArgumentCaptor<CalcomEventType> captor = ArgumentCaptor.forClass(CalcomEventType.class);
    verify(calcomEventTypeService).updateEventType(captor.capture(), Mockito.eq(Lists.newArrayList("location")));
    assertThat(captor.getValue().getMemberIds()).contains(CALCOM_USER_ID);
  }

  @Test
  void deleteAgencyEventType_Should_CallDeleteEventType() {
    // when
    agencyFacade.deleteAgencyEventType(EVENT_TYPE_ID);
    // then
    verify(calcomEventTypeService).deleteEventType(EVENT_TYPE_ID);
  }

  @Test
  void getAgencyEventTypeById_Should_GetAndAttachCalcomInformationToEventType() {
    // given
    CalcomEventType eventType = new CalcomEventType();
    eventType.setSlug("eventTypeSlug");
    eventType.setTeamId(CALCOM_TEAM_ID);
    eventType.setMemberIds(Lists.newArrayList(CALCOM_USER_ID));
    when(calcomEventTypeService.getEventTypeById(EVENT_TYPE_ID)).thenReturn(eventType);
    when(user2ConsultantRepo.findByCalComUserId(CALCOM_USER_ID)).thenReturn(Optional.of(new CalcomUserToConsultant()));
    CalcomUser calcomUser = new CalcomUser();
    calcomUser.setName("consultant name");
    when(calComUserService.getUserById(CALCOM_USER_ID)).thenReturn(calcomUser);
    var team = giveCalcomTeam();
    CalcomTeam calcomTeam = new CalcomTeam();
    calcomTeam.setSlug("calcomSlug");
    when(calComTeamService.getTeamById(CALCOM_TEAM_ID)).thenReturn(calcomTeam);

    // when
    var result = agencyFacade.getAgencyEventTypeById(EVENT_TYPE_ID);
    // then

    assertThat(result.getConsultants()).hasSize(1);
    assertThat(result.getConsultants()).extracting("consultantName").contains("consultant name");
    assertThat(result.getSlug()).isEqualTo("calcomSlug/eventTypeSlug");
  }

  private static CalcomUserToConsultant giveCalcomUserToConsultant() {
    CalcomUserToConsultant calcomUser = new CalcomUserToConsultant();
    calcomUser.setConsultantId(CONSULTANT_ID);
    calcomUser.setCalComUserId(CALCOM_USER_ID);
    return calcomUser;
  }
}