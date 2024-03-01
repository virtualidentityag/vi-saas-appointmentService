package com.vi.appointmentservice.api.calcom.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@TestPropertySource(properties = "spring.profiles.active=testing")
@AutoConfigureTestDatabase(replace = Replace.ANY)
@ExtendWith(SpringExtension.class)
@SpringBootTest
class ScheduleRepositoryTest {

  @Autowired
  ScheduleRepository scheduleRepository;

  @Autowired
  JdbcTemplate jdbcTemplate;

  @Before
  public void setUp() {
   jdbcTemplate.execute("DROP TABLE IF EXISTS \"Schedule\"");
  }
  @Test
  void deleteUserSchedules_Should_DeleteSchedulesPerUserId() {
    // given
    inititalizeDB();

    jdbcTemplate.execute("INSERT INTO \"Schedule\" (\"id\", \"userId\", \"name\") VALUES (1, 1, 'DEFAULT_SCHEDULE')");
    jdbcTemplate.execute("INSERT INTO \"Schedule\" (\"id\", \"userId\", \"name\") VALUES (2, 1, 'DEFAULT_SCHEDULE')");
    // when
    Set<Integer> integers = scheduleRepository.deleteUserSchedules(1L);
    // then
    assertThat(integers).containsOnly(1, 2);

  }

  private void inititalizeDB() {
    // we can't use @Sql annotation here because it's not visible in jdbcTemplate,
    // probably because there are defined multiple jdbc templates in this project for different datasources
    jdbcTemplate.execute("create table  \"Schedule\"\n"
        + "(\n"
        + "    \"id\"          integer not null\n"
        + "        primary key,\n"
        + "    \"userId\"      integer not null,\n"
        + "    \"name\"       varchar(255) not null\n"
        + ");");
  }

}