package com.whizzosoftware.kpush.listener;

import com.whizzosoftware.kpush.event.CreateDeploymentEvent;
import com.whizzosoftware.kpush.event.ImageDeployStatusEvent;
import com.whizzosoftware.kpush.event.UpdateDeploymentEvent;
import com.whizzosoftware.kpush.k8s.DeploymentHelper;
import com.whizzosoftware.kpush.manager.DeploymentManager;
import com.whizzosoftware.kpush.manager.ImageManager;
import com.whizzosoftware.kpush.model.Image;
import com.whizzosoftware.kpush.model.ImageDeploy;
import io.kubernetes.client.models.V1Container;
import io.kubernetes.client.models.V1Deployment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

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
            Collection<Image> images = imageManager.getImages(DeploymentHelper.getAllImageRefs(id.getSpec().getDeployment()));
            Map<String,String> imageStateMap = new HashMap<>();
            for (Image i : images) {
                imageStateMap.put(i.getId(), i.getLatest());
            }

            if (!images.isEmpty()) {
                logger.debug("Found Image resources for ImageDeploy: {}", id.getMetadata().getName());

                // create new deployment object
                V1Deployment newDeployment = id.getSpec().getDeployment();

                // check for existing K8S deployment
                V1Deployment d = deployManager.getDeployment(id.getSpec().getDeployment().getMetadata().getNamespace(), id.getSpec().getDeployment().getMetadata().getName());
                if (d != null) {
                    logger.debug("Found Deployment referenced by ImageDeploy {}: {}", id.getSpec().getDeployment().getMetadata().getName(), d.getMetadata().getName());
                    Collection<V1Container> specContainers = DeploymentHelper.getAllContainers(id.getSpec().getDeployment());
                    for (V1Container c : specContainers) {
                        if (DeploymentHelper.isImageRef(c.getImage())) {
                            V1Container c2 = DeploymentHelper.getContainerWithName(d, c.getName());
                            if (c2 != null && !c2.getImage().equals(imageStateMap.get(DeploymentHelper.decodeImageRef(c.getImage())))) {
                                DeploymentHelper.replaceAllImageRefs(newDeployment, imageStateMap);
                                publisher.publishEvent(new UpdateDeploymentEvent(this, newDeployment));
                                break;
                            }
                        }
                    }
                } else {
                    logger.debug("No Deployment referenced by ImageDeploy {} found", id.getSpec().getDeployment().getMetadata().getName());
                    DeploymentHelper.replaceAllImageRefs(newDeployment, imageStateMap);
                    publisher.publishEvent(new CreateDeploymentEvent(this, newDeployment));
                }
            } else {
                logger.debug("No Image resources found for ImageDeploy");
            }
        }
    }
}
