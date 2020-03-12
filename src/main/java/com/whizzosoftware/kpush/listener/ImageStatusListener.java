package com.whizzosoftware.kpush.listener;

import com.whizzosoftware.kpush.event.CreateDeploymentEvent;
import com.whizzosoftware.kpush.event.ImageStatusEvent;
import com.whizzosoftware.kpush.event.UpdateDeploymentEvent;
import com.whizzosoftware.kpush.k8s.DeploymentHelper;
import com.whizzosoftware.kpush.manager.DeploymentManager;
import com.whizzosoftware.kpush.manager.ImageDeployManager;
import com.whizzosoftware.kpush.model.Image;
import com.whizzosoftware.kpush.model.ImageDeploy;
import io.kubernetes.client.models.V1Container;
import io.kubernetes.client.models.V1Deployment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class ImageStatusListener implements ApplicationListener<ImageStatusEvent> {
    private final Logger logger = LoggerFactory.getLogger(ImageStatusListener.class);

    private ImageDeployManager imageDeployManager;
    private DeploymentManager deploymentManager;
    private ApplicationEventPublisher eventPublisher;

    public ImageStatusListener(ImageDeployManager imageDeployManager, DeploymentManager deploymentManager, ApplicationEventPublisher eventPublisher) {
        this.imageDeployManager = imageDeployManager;
        this.deploymentManager = deploymentManager;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void onApplicationEvent(ImageStatusEvent event) {
        logger.trace("Received event: {}", event);

        Image image = event.getImage();

        Collection<ImageDeploy> imageDeploys = imageDeployManager.getImageDeploysForImageName(image.getName());
        logger.debug("Found {} ImageDeploy resource(s) with reference to Image {}", imageDeploys.size(), image.getName());
        for (ImageDeploy id : imageDeploys) {
            V1Deployment d = deploymentManager.getDeployment(id.getSpec().getDeployment().getMetadata().getNamespace(), id.getSpec().getDeployment().getMetadata().getName());
            if (d != null) {
                logger.debug("Found Deployment referenced by ImageDeploy {}: {}", id.getSpec().getDeployment().getMetadata().getName(), d.getMetadata().getName());
                List<String> targetContainerNames = new ArrayList<>();
                // iterate through all containers that reference the changed image id...
                for (V1Container c : id.getContainersWithImageRef(image.getName())) {
                    // ...and build a list of those that are not updated to the latest image
                    V1Container c2 = DeploymentHelper.getContainerWithName(d, c.getName());
                    if (c2 != null && !image.getLatest().equals(c2.getImage())) {
                        targetContainerNames.add(c.getName());
                    }
                }
                if (targetContainerNames.size() > 0) {
                    logger.debug("Publishing update deployment event");
                    V1Deployment newDeployment = id.getSpec().getDeployment();
                    DeploymentHelper.replaceAllImageRefs(newDeployment, Collections.singletonMap(image.getName(), image.getLatest()));
                    eventPublisher.publishEvent(new UpdateDeploymentEvent(this, newDeployment));
                } else {
                    logger.debug("Image({}) and Deployment({}) images match: {}", image.getName(), d.getMetadata().getName(), image.getLatest());
                }
            } else {
                logger.debug("Publishing create deployment event");
                V1Deployment newDeployment = id.getSpec().getDeployment();
                DeploymentHelper.replaceAllImageRefs(newDeployment, Collections.singletonMap(image.getName(), image.getLatest()));
                eventPublisher.publishEvent(new CreateDeploymentEvent(this, newDeployment));
            }
        }
    }
}
