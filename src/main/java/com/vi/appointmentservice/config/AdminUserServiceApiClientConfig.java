package com.vi.appointmentservice.config;

import com.vi.appointmentservice.useradminservice.generated.ApiClient;
import com.vi.appointmentservice.useradminservice.generated.web.AdminUserControllerApi;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class AdminUserServiceApiClientConfig {

  @Value("${user.admin.service.api.url}")
  private String adminUserServiceApiUrl;

  @Bean(name = "adminUser")
  public AdminUserControllerApi adminUserControllerApi(RestTemplate restTemplate) {
    ApiClient apiClient = new AdminUserApiClient(restTemplate);
    apiClient.setBasePath(this.adminUserServiceApiUrl);
    return new AdminUserControllerApi(apiClient);
  }

}