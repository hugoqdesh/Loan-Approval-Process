package com.example.loanapprovalprocess.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class ApplicationConfiguration {

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    public OpenAPI loanApprovalProcessOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Loan Approval Process API")
                        .version("v1")
                        .description("Backend API for submitting, scheduling, and reviewing loan applications."));
    }
}
