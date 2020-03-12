package com.whizzosoftware.kpush.config;

import io.kubernetes.client.ApiClient;
import io.kubernetes.client.util.ClientBuilder;
import io.kubernetes.client.util.KubeConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.FileReader;
import java.io.IOException;

@Configuration
@EnableScheduling
public class AppConfiguration {
    @Bean
    public ApiClient apiClient() {
        try {
            return ClientBuilder.kubeconfig(KubeConfig.loadKubeConfig(new FileReader("/Users/dnoguerol/.kube/config"))).build();
        } catch (IOException e) {
            return new ApiClient();
        }
    }
}
