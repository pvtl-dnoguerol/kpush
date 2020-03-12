package com.whizzosoftware.kpush.listener;

import com.whizzosoftware.kpush.event.CreateDeploymentEvent;
import com.whizzosoftware.kpush.event.ImageDeployStatusEvent;
import com.whizzosoftware.kpush.event.UpdateDeploymentEvent;
import com.whizzosoftware.kpush.manager.DeploymentManager;
import com.whizzosoftware.kpush.manager.ImageManager;
import com.whizzosoftware.kpush.model.Image;
import com.whizzosoftware.kpush.model.ImageDeploy;

import static com.whizzosoftware.kpush.k8s.DeploymentHelper.*;

import io.kubernetes.client.models.V1Container;
import io.kubernetes.client.models.V1Deployment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ImageDeployStatusListener implements ApplicationListener<ImageDeployStatusEvent> {
    private final Logger logger = LoggerFactory.getLogger(ImageDeployStatusListener.class);

    private ImageManager imageManager;
    private DeploymentManager deployManager;
    private ApplicationEventPublisher publisher;

    public ImageDeployStatusListener(ImageManager imageManager, DeploymentManager deployManager, ApplicationEventPublisher publisher) {
        this.imageManager = imageManager;
        this.deployManager = deployManager;
        this.publisher = publisher;
    }

    @Override
    public void onApplicationEvent(ImageDeployStatusEvent event) {
        logger.trace("Received event: {}", event);

        ImageDeploy id = event.getImageDeploy();
        if (id != null) {
            // build a map of image id -> container image version for all images referenced by the imagedeploy
            Collection<Image> images = imageManager.getImages(getAllImageRefs(id.getSpec().getDeployment()));
            Map<String, String> imageStateMap = images.stream().collect(Collectors.toMap(Image::getName, Image::getLatest));

            // if images were found...
            if (!imageStateMap.isEmpty()) {
                logger.debug("Found Image resources for ImageDeploy: {}", id.getMetadata().getName());
                // check if a deployment already exists with the name referenced by the imagedeploy
                V1Deployment currentDeployment = deployManager.getDeployment(id.getSpec().getDeployment().getMetadata().getNamespace(), id.getSpec().getDeployment().getMetadata().getName());
                // if so, check if it needs to be updated
                if (currentDeployment != null) {
                    logger.debug("Found Deployment referenced by ImageDeploy {}: {}", id.getSpec().getDeployment().getMetadata().getName(), currentDeployment.getMetadata().getName());
                    // get all the containers defined by the imagedeploy deployment definition
                    Collection<V1Container> specContainers = getAllContainers(id.getSpec().getDeployment());
                    boolean updated = false;
                    for (V1Container specContainer : specContainers) {
                        // retrieve the container with the same name from the current deployment
                        V1Container currentContainer = getContainerWithName(currentDeployment, specContainer.getName());
                        // if the image name of the current container is different from the spec one, update the deployment
                        String imageName = (isImageRef(specContainer.getImage())) ? imageStateMap.get(decodeImageRef(specContainer.getImage())) : specContainer.getImage();
                        if (currentContainer != null && !currentContainer.getImage().equals(imageName)) {
                            replaceAllImageRefs(id.getSpec().getDeployment(), imageStateMap);
                            publisher.publishEvent(new UpdateDeploymentEvent(this, id.getSpec().getDeployment()));
                            updated = true;
                            break;
                        }
                    }
                    if (!updated) {
                        logger.debug("Deployment({}) matches referenced Image resources: {}", currentDeployment.getMetadata().getName(), imageStateMap);
                    }
                // if not, create a new one
                } else {
                    logger.debug("No Deployment referenced by ImageDeploy {} found", id.getSpec().getDeployment().getMetadata().getName());
                    replaceAllImageRefs(id.getSpec().getDeployment(), imageStateMap);
                    publisher.publishEvent(new CreateDeploymentEvent(this, id.getSpec().getDeployment()));
                }
            } else {
                logger.debug("No Image resources found for ImageDeploy");
            }
        }
    }
}
