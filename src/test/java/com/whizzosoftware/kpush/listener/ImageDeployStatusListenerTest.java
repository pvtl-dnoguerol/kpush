package com.whizzosoftware.kpush.listener;

import com.whizzosoftware.kpush.TestModelHelper;
import com.whizzosoftware.kpush.MockApplicationEventPublisher;
import com.whizzosoftware.kpush.event.CreateDeploymentEvent;
import com.whizzosoftware.kpush.event.ImageDeployStatusEvent;
import com.whizzosoftware.kpush.event.UpdateDeploymentEvent;
import com.whizzosoftware.kpush.k8s.DeploymentHelper;
import com.whizzosoftware.kpush.manager.MockDeploymentManager;
import com.whizzosoftware.kpush.manager.MockImageManager;
import com.whizzosoftware.kpush.model.Image;
import com.whizzosoftware.kpush.model.ImageDeploy;
import io.kubernetes.client.models.V1DeploymentBuilder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ImageDeployStatusListenerTest {
    @Test
    public void testNewImageDeployWithNoImage() {
        MockImageManager manager = new MockImageManager();
        MockApplicationEventPublisher publisher = new MockApplicationEventPublisher();
        ImageDeployStatusListener listener = new ImageDeployStatusListener(manager, new MockDeploymentManager(), publisher);
        assertEquals(0, publisher.getPublishedEventCount());
        listener.onApplicationEvent(new ImageDeployStatusEvent(this, TestModelHelper.createImageDeploy("default", "myimagedeploy", "mydeploy", "myImageName", 1)));
        assertEquals(0, publisher.getPublishedEventCount());
    }

    @Test
    public void testNewImageDeployWithImageAndNoDeployment() {
        MockImageManager manager = new MockImageManager();
        manager.addImage(new Image("image1", "latest"));
        MockApplicationEventPublisher publisher = new MockApplicationEventPublisher();
        ImageDeployStatusListener listener = new ImageDeployStatusListener(manager, new MockDeploymentManager(), publisher);
        assertEquals(0, publisher.getPublishedEventCount());

        listener.onApplicationEvent(new ImageDeployStatusEvent(this, new ImageDeploy().
                withNewMetadata().
                withNamespace("default").
                withName("imagedeploy1").
                endMetadata().
                withNewSpec().
                withDeployment(new V1DeploymentBuilder().
                        withNewMetadata().
                        withNamespace("default").
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
                        endSpec().build()
                ).
                endSpec()
        ));

        assertEquals(1, publisher.getPublishedEventCount());
        assertTrue(publisher.getEvent(0) instanceof CreateDeploymentEvent);
        CreateDeploymentEvent event = (CreateDeploymentEvent)publisher.getEvent(0);
        assertEquals("default", event.getDeployment().getMetadata().getNamespace());
        assertEquals("deploy1", event.getDeployment().getMetadata().getName());
        assertEquals("latest", event.getDeployment().getSpec().getTemplate().getSpec().getContainers().get(0).getImage());
    }

    @Test
    public void testNewImageDeployWithImageAndExistingDeploymentWithOldImage() {
        MockImageManager imageManager = new MockImageManager();
        imageManager.addImage(new Image("image1", "latest"));

        MockDeploymentManager deployManager = new MockDeploymentManager();
        deployManager.addDeployment(new V1DeploymentBuilder().
                withNewMetadata().
                    withNamespace("default").
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

        ImageDeployStatusListener listener = new ImageDeployStatusListener(imageManager, deployManager, publisher);
        assertEquals(0, publisher.getPublishedEventCount());
        listener.onApplicationEvent(new ImageDeployStatusEvent(this, new ImageDeploy().
                withNewMetadata().
                    withNamespace("default").
                    withName("imagedeploy1").
                endMetadata().
                withNewSpec().
                    withDeployment(new V1DeploymentBuilder().
                        withNewMetadata().
                            withNamespace("default").
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
                        endSpec().build()
                    ).
                endSpec()
        ));

        assertEquals(1, publisher.getPublishedEventCount());
        assertTrue(publisher.getEvent(0) instanceof UpdateDeploymentEvent);
        UpdateDeploymentEvent event = (UpdateDeploymentEvent)publisher.getEvent(0);
        assertEquals("default", event.getDeployment().getMetadata().getNamespace());
        assertEquals("deploy1", event.getDeployment().getMetadata().getName());
    }

    @Test
    public void testNewImageDeployWithImageAndExistingDeploymentWithSameImage() {
        MockImageManager imageManager = new MockImageManager();
        imageManager.addImage(new Image("myimage", "latest"));
        MockDeploymentManager deployManager = new MockDeploymentManager();
        deployManager.addDeployment(TestModelHelper.createDeployment("default", "mydeploy", "latest", 1));
        MockApplicationEventPublisher publisher = new MockApplicationEventPublisher();

        ImageDeployStatusListener listener = new ImageDeployStatusListener(imageManager, deployManager, publisher);
        assertEquals(0, publisher.getPublishedEventCount());
        listener.onApplicationEvent(new ImageDeployStatusEvent(this, TestModelHelper.createImageDeploy( "default","myimagedeploy", "mydeploy", "myImageName", 1)));
        assertEquals(0, publisher.getPublishedEventCount());
    }
}
