package com.whizzosoftware.kpush.config;

import io.kubernetes.client.ApiClient;
import io.kubernetes.client.util.Config;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableScheduling
@EnableAsync
public class AppConfiguration {
    @Bean
    public ApiClient apiClient() {
        try {
            ApiClient client = Config.defaultClient();
            // https://github.com/kubernetes-client/java/issues/150
            client.getHttpClient().setReadTimeout(5, TimeUnit.MINUTES);
            return client;
        } catch (IOException e) {
            return new ApiClient();
        }
    }

    @Bean("threadPoolTaskExecutor")
    public TaskExecutor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(2);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setThreadNamePrefix("Async-");
        return executor;
    }
}
