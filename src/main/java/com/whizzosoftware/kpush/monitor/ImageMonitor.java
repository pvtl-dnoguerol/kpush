package com.whizzosoftware.kpush.monitor;

import com.whizzosoftware.kpush.event.ImageStatusEvent;
import com.whizzosoftware.kpush.manager.ImageManager;
import com.whizzosoftware.kpush.model.Image;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
public class ImageMonitor {
    private final Logger logger = LoggerFactory.getLogger(ImageMonitor.class);

    private ImageManager manager;
    private ApplicationEventPublisher publisher;

    public ImageMonitor(ImageManager manager, ApplicationEventPublisher publisher) {
        this.manager = manager;
        this.publisher = publisher;
    }

    @Scheduled(initialDelay = 10000, fixedRate = 20000)
    public void poll() {
        logger.debug("Polling for Image resources");
        Collection<Image> results = manager.getAllImages();
        logger.debug("Found {} total Image resource(s)", results.size());
        for (Image i : results) {
            publisher.publishEvent(new ImageStatusEvent(this, i));
        }
    }
}
