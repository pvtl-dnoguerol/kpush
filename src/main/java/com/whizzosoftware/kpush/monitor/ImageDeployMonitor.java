package com.whizzosoftware.kpush.monitor;

import com.whizzosoftware.kpush.event.ImageDeployStatusEvent;
import com.whizzosoftware.kpush.manager.k8s.K8SImageDeployManager;
import com.whizzosoftware.kpush.model.ImageDeploy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
public class ImageDeployMonitor {
    private final Logger logger = LoggerFactory.getLogger(ImageDeployMonitor.class);

    private K8SImageDeployManager manager;
    private ApplicationEventPublisher publisher;

    public ImageDeployMonitor(K8SImageDeployManager manager, ApplicationEventPublisher publisher) {
        this.manager = manager;
        this.publisher = publisher;
    }

    @Scheduled(fixedRate = 20000)
    public void poll() {
        logger.debug("Polling for ImageDeploy resources");
        Collection<ImageDeploy> results = manager.getAllImageDeploys();
        logger.debug("Found {} total ImageDeploy resource(s)", results.size());
        for (ImageDeploy id : results) {
            publisher.publishEvent(new ImageDeployStatusEvent(this, id));
        }
    }
}
