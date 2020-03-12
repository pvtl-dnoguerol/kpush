package com.whizzosoftware.kpush.config;

import io.kubernetes.client.ApiClient;
import io.kubernetes.client.util.Config;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.IOException;

@Configuration
@EnableScheduling
public class AppConfiguration {
    @Bean
    public ApiClient apiClient() {
        try {
            return Config.defaultClient();
        } catch (IOException e) {
            return new ApiClient();
        }
    }
}
