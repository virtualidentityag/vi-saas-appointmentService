package com.vi.appointmentservice.config;

import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

@Configuration
public class DatabaseConnectionsConfiguration {

  @Value("${spring.datasource.url}")
  private String url;

  @Value("${spring.datasource.username}")
  private String username;

  @Value("${spring.datasource.password}")
  private String password;

  @Value("${spring.datasource.driver-class-name}")
  private String className;

  @Bean
  @Primary
  public DataSource dataSource() {
    return DataSourceBuilder.create().url(url).username(username).password(password)
        .driverClassName(className).build();
  }

  @Bean(name = "calcomDBDataSource")
  public DataSource calcomDBDataSource() {
    return DataSourceBuilder.create().url("jdbc:postgresql://185.201.145.213:5432/calendso")
        .username("calendso").password("Testtest!12").driverClassName("org.postgresql.Driver")
        .build();
  }

  @Bean(name = "calcomDBTemplate")
  public JdbcTemplate calcomDBTemplate(@Qualifier("calcomDBDataSource") DataSource calcomDBDataSource){
    var template = new JdbcTemplate();
    template.setDataSource(calcomDBDataSource);
    return template;
  }

  @Bean(name = "calcomDBNamedParamterTemplate")
  public NamedParameterJdbcTemplate calcomDBNamedParamterTemplate(@Qualifier("calcomDBDataSource") DataSource calcomDBDataSource){
    return new NamedParameterJdbcTemplate(calcomDBDataSource);
  }

}
