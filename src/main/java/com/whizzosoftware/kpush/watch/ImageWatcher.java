package com.whizzosoftware.kpush.watch;

import com.google.gson.reflect.TypeToken;
import com.whizzosoftware.kpush.event.ImageStatusEvent;
import com.whizzosoftware.kpush.manager.k8s.K8SImageManager;
import com.whizzosoftware.kpush.model.Image;
import io.kubernetes.client.ApiClient;
import io.kubernetes.client.apis.CustomObjectsApi;
import io.kubernetes.client.util.Watch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class ImageWatcher {
    private final Logger logger = LoggerFactory.getLogger(ImageWatcher.class);

    @Value("${IMAGE_CRD_GROUP:build.pivotal.io}")
    public String CRD_GROUP;
    @Value("${IMAGE_CRD_VERSION:v1alpha1}")
    public String CRD_VERSION;
    @Value("${IMAGE_CRD_PLURAL:images}")
    public String CRD_PLURAL;

    private ApiClient api;
    private CustomObjectsApi coApi;
    private ApplicationEventPublisher publisher;

    public ImageWatcher(ApiClient api, ApplicationEventPublisher publisher) {
        this.api = api;
        this.coApi = new CustomObjectsApi(api);
        this.publisher = publisher;

        logger.debug("Setting up image watch");
    }

    @Async("threadPoolTaskExecutor")
    @Scheduled(initialDelay = 0, fixedDelay=Long.MAX_VALUE)
    public void startWatch() {
        try {
            while (true) {
                Watch<Image> watch = Watch.createWatch(
                        api,
                        coApi.listClusterCustomObjectCall(CRD_GROUP, CRD_VERSION, CRD_PLURAL, null, null, null, null, null, Boolean.TRUE, null, null),
                        new TypeToken<Watch.Response<Image>>() {
                        }.getType()
                );

                try {
                    logger.debug("Watching for images");
                    for (Watch.Response<Image> item : watch) {
                        logger.info("Found change in Image: {}", item.object.getMetadata().getName());
                        publisher.publishEvent(new ImageStatusEvent(this, item.object));
                    }
                } catch (RuntimeException e) {
                    logger.trace("Wait timeout");
                } finally {
                    try {
                        watch.close();
                    } catch (IOException e) {
                        logger.trace("Error closing image watch", e);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error setting up image watch", e);
        }
    }
}
