package com.whizzosoftware.kpush.listener;

import com.whizzosoftware.kpush.MockApplicationEventPublisher;
import com.whizzosoftware.kpush.TestModelHelper;
import com.whizzosoftware.kpush.event.CreateDeploymentEvent;
import com.whizzosoftware.kpush.event.ImageStatusEvent;
import com.whizzosoftware.kpush.event.UpdateDeploymentEvent;
import com.whizzosoftware.kpush.k8s.DeploymentHelper;
import com.whizzosoftware.kpush.manager.MockDeploymentManager;
import com.whizzosoftware.kpush.manager.MockImageDeployManager;
import com.whizzosoftware.kpush.model.Image;
import com.whizzosoftware.kpush.model.ImageDeploy;
import com.whizzosoftware.kpush.model.ImageDeploy.Metadata;
import com.whizzosoftware.kpush.model.ImageDeploy.Spec;
import io.kubernetes.client.models.V1DeploymentBuilder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ImageStatusListenerTest {
    @Test
    public void testImageUpdatedWithUnmonitoredImageTag() {
        MockImageDeployManager manager = new MockImageDeployManager();
        manager.addImageDeploy(TestModelHelper.createImageDeploy("default", "imagedeploy", "deploy1", "image1", 1));
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
        imageDeliveryManager.addImageDeploy(TestModelHelper.createImageDeploy("default", "imagedeploy", "deploy1", "image", 1));
        MockDeploymentManager deploymentManager = new MockDeploymentManager();
        deploymentManager.addDeployment(TestModelHelper.createDeployment("default", "deploy1", "latest", 1));
        MockApplicationEventPublisher publisher = new MockApplicationEventPublisher();
        ImageStatusListener listener = new ImageStatusListener(imageDeliveryManager, deploymentManager, publisher);
        assertEquals(0, publisher.getPublishedEventCount());
        listener.onApplicationEvent(new ImageStatusEvent(this, new Image("image", "latest")));
        assertEquals(0, publisher.getPublishedEventCount());
    }

    @Test
    public void testImageUpdatedWithMonitoredImageTagAndExistingDeploymentAndImageChange() {
        MockImageDeployManager imageDeployManager = new MockImageDeployManager();

        imageDeployManager.addImageDeploy(new ImageDeploy()
                .setMetadata(new Metadata()
                        .setNamespace("namespace")
                        .setName("imagedeploy1"))
                .setSpec(new Spec().setDeployment(new V1DeploymentBuilder().
                        withNewMetadata().
                        withNamespace("namespace").
                        withName("deploy1").
                        endMetadata().
                        withNewSpec().
                        withNewTemplate().
                        withNewSpec().
                        withContainers().
                        addNewContainer().
                        withName("container1").
                        withImage(DeploymentHelper.encodeImageRef("image1")).
                        endContainer().
                        endSpec().
                        endTemplate().
                        endSpec().build()))
        );


        MockDeploymentManager deploymentManager = new MockDeploymentManager();
        deploymentManager.addDeployment(new V1DeploymentBuilder().
                withNewMetadata().
                withNamespace("namespace").
                withName("deploy1").
                endMetadata().
                withNewSpec().
                withNewTemplate().
                withNewSpec().
                withContainers().
                addNewContainer().
                withName("container1").
                withImage("old").
                endContainer().
                endSpec().
                endTemplate().
                endSpec().build()
        );

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
        imageDeliveryManager.addImageDeploy(TestModelHelper.createImageDeploy("default", "image1", "deploy1", "image1", 1));
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
        //assertEquals("latest", event.getImage());
    }
}
