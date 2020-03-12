package com.whizzosoftware.kpush.listener;

import com.whizzosoftware.kpush.MockApplicationEventPublisher;
import com.whizzosoftware.kpush.event.CreateDeploymentEvent;
import com.whizzosoftware.kpush.event.ImageStatusEvent;
import com.whizzosoftware.kpush.event.UpdateDeploymentEvent;
import com.whizzosoftware.kpush.manager.MockDeploymentManager;
import com.whizzosoftware.kpush.manager.MockImageDeployManager;
import com.whizzosoftware.kpush.model.Image;

import static com.whizzosoftware.kpush.TestModelHelper.*;

import org.junit.jupiter.api.Test;

import static com.whizzosoftware.kpush.k8s.DeploymentHelper.encodeImageRef;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ImageStatusListenerTest {
    @Test
    public void testImageUpdatedWithUnmonitoredImageTag() {
        MockImageDeployManager manager = new MockImageDeployManager();
        manager.addImageDeploy(createImageDeploy(
                "default",
                "imagedeploy",
                createDeploymentWithOneContainer(
                        "default",
                        "deploy1",
                        "mycontainer",
                        "image1",
                        1
                )
        ));
        MockDeploymentManager deploymentManager = new MockDeploymentManager();
        MockApplicationEventPublisher publisher = new MockApplicationEventPublisher();
        ImageStatusListener listener = new ImageStatusListener(manager, deploymentManager, publisher);
        assertEquals(0, publisher.getPublishedEventCount());
        listener.onApplicationEvent(new ImageStatusEvent(this, new Image("image", "latest")));
        assertEquals(0, publisher.getPublishedEventCount());
    }

    @Test
    public void testImageUpdatedWithMonitoredImageTagAndExistingDeploymentAndNoImageChange() {
        MockImageDeployManager imageDeliveryManager = new MockImageDeployManager();
        imageDeliveryManager.addImageDeploy(createImageDeploy(
                "default",
                "imagedeploy",
                createDeploymentWithOneContainer(
                        "default",
                        "deploy1",
                        "mycontainer",
                        encodeImageRef("image"),
                        1
                )
        ));
        MockDeploymentManager deploymentManager = new MockDeploymentManager();
        deploymentManager.addDeployment(createDeploymentWithOneContainer(
                "default",
                "deploy1",
                "mycontainer",
                "latest",
                1
        ));
        MockApplicationEventPublisher publisher = new MockApplicationEventPublisher();
        ImageStatusListener listener = new ImageStatusListener(imageDeliveryManager, deploymentManager, publisher);
        assertEquals(0, publisher.getPublishedEventCount());
        listener.onApplicationEvent(new ImageStatusEvent(this, new Image("image", "latest")));
        assertEquals(0, publisher.getPublishedEventCount());
    }

    @Test
    public void testImageUpdatedWithMonitoredImageTagAndExistingDeploymentAndImageChange() {
        MockImageDeployManager imageDeployManager = new MockImageDeployManager();

        imageDeployManager.addImageDeploy(createImageDeploy(
                "namespace",
                "imagedeploy1",
                createDeploymentWithOneContainer(
                        "namespace",
                        "deploy1",
                        "container1",
                        encodeImageRef("image1"),
                        1
                )
        ));

        MockDeploymentManager deploymentManager = new MockDeploymentManager();
        deploymentManager.addDeployment(createDeploymentWithOneContainer(
                "namespace",
                "deploy1",
                "container1",
                "old",
                1
        ));

        MockApplicationEventPublisher publisher = new MockApplicationEventPublisher();
        ImageStatusListener listener = new ImageStatusListener(imageDeployManager, deploymentManager, publisher);
        assertEquals(0, publisher.getPublishedEventCount());
        listener.onApplicationEvent(new ImageStatusEvent(this, new Image("image1", "latest")));
        assertEquals(1, publisher.getPublishedEventCount());
        assertTrue(publisher.getEvent(0) instanceof UpdateDeploymentEvent);
        UpdateDeploymentEvent event = (UpdateDeploymentEvent) publisher.getEvent(0);
        assertEquals("namespace", event.getDeployment().getMetadata().getNamespace());
        assertEquals("deploy1", event.getDeployment().getMetadata().getName());
    }

    @Test
    public void testImageUpdatedWithMonitoredImageTagAndNoDeploymentAndImageChange() {
        MockImageDeployManager imageDeliveryManager = new MockImageDeployManager();
        imageDeliveryManager.addImageDeploy(createImageDeploy(
                "default",
                "image1",
                createDeploymentWithOneContainer(
                        "default",
                        "deploy1",
                        "mycontainer",
                        encodeImageRef("image1"),
                        1
                )
        ));

        MockDeploymentManager deploymentManager = new MockDeploymentManager();

        MockApplicationEventPublisher publisher = new MockApplicationEventPublisher();

        ImageStatusListener listener = new ImageStatusListener(imageDeliveryManager, deploymentManager, publisher);
        assertEquals(0, publisher.getPublishedEventCount());
        listener.onApplicationEvent(new ImageStatusEvent(this, new Image("image1", "latest")));
        assertEquals(1, publisher.getPublishedEventCount());
        assertTrue(publisher.getEvent(0) instanceof CreateDeploymentEvent);
        CreateDeploymentEvent event = (CreateDeploymentEvent) publisher.getEvent(0);
        assertEquals("default", event.getDeployment().getMetadata().getNamespace());
        assertEquals("deploy1", event.getDeployment().getMetadata().getName());
        assertEquals("latest", event.getDeployment().getSpec().getTemplate().getSpec().getContainers().get(0).getImage());
    }
}
