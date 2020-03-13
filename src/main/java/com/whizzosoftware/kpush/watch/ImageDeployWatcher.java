package com.whizzosoftware.kpush.watch;

import com.google.gson.reflect.TypeToken;
import com.whizzosoftware.kpush.event.ImageDeployStatusEvent;
import com.whizzosoftware.kpush.model.ImageDeploy;
import io.kubernetes.client.ApiClient;
import io.kubernetes.client.apis.CustomObjectsApi;
import io.kubernetes.client.util.Watch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class ImageDeployWatcher {
    private final Logger logger = LoggerFactory.getLogger(ImageDeployWatcher.class);

    public static final String CRD_GROUP = "crd.whizzosoftware.com";
    public static final String CRD_VERSION = "v1alpha1";
    public static final String CRD_PLURAL = "imagedeploys";

    private ApiClient api;
    private CustomObjectsApi coApi;
    private ApplicationEventPublisher publisher;

    public ImageDeployWatcher(ApiClient api, ApplicationEventPublisher publisher) {
        this.api = api;
        this.coApi = new CustomObjectsApi(api);
        this.publisher = publisher;

        logger.debug("Setting up image deploy watch");
    }

    @Async("threadPoolTaskExecutor")
    @Scheduled(initialDelay = 0, fixedDelay=Long.MAX_VALUE)
    public void startWatch() {
        try {
            while (true) {
                Watch<ImageDeploy> watch = Watch.createWatch(
                    api,
                    coApi.listClusterCustomObjectCall(CRD_GROUP, CRD_VERSION, CRD_PLURAL, null, null, null, null, null, Boolean.TRUE, null, null),
                    new TypeToken<Watch.Response<ImageDeploy>>() {
                    }.getType()
                );

                try {
                    logger.debug("Watching for image deploys");
                    for (Watch.Response<ImageDeploy> item : watch) {
                        logger.info("Found change in ImageDeploy: {}", item.object.getMetadata().getName());
                        publisher.publishEvent(new ImageDeployStatusEvent(this, item.object));
                    }
                } catch (RuntimeException e) {
                    logger.trace("Wait timeout");
                } finally {
                    try {
                        watch.close();
                    } catch (IOException e) {
                        logger.trace("Error closing image deploy watch", e);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error setting up image deploy watch", e);
        }
    }
}
