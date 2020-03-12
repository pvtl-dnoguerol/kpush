package com.whizzosoftware.kpush.listener;

import com.whizzosoftware.kpush.MockApplicationEventPublisher;
import com.whizzosoftware.kpush.event.CreateDeploymentEvent;
import com.whizzosoftware.kpush.event.ImageDeployStatusEvent;
import com.whizzosoftware.kpush.event.UpdateDeploymentEvent;
import com.whizzosoftware.kpush.manager.MockDeploymentManager;
import com.whizzosoftware.kpush.manager.MockImageManager;
import com.whizzosoftware.kpush.model.Image;

import static com.whizzosoftware.kpush.TestModelHelper.*;

import org.junit.jupiter.api.Test;

import static com.whizzosoftware.kpush.k8s.DeploymentHelper.encodeImageRef;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ImageDeployStatusListenerTest {
    @Test
    public void testNewImageDeployWithNoImage() {
        MockImageManager manager = new MockImageManager();
        MockApplicationEventPublisher publisher = new MockApplicationEventPublisher();
        ImageDeployStatusListener listener = new ImageDeployStatusListener(manager, new MockDeploymentManager(), publisher);
        assertEquals(0, publisher.getPublishedEventCount());
        listener.onApplicationEvent(new ImageDeployStatusEvent(
                this,
                createImageDeploy(
                        "default",
                        "myimagedeploy",
                        createDeploymentWithOneContainer(
                                "default",
                                "mydeploy",
                                "myImage",
                                "myImageName",
                                1)
                )));
        assertEquals(0, publisher.getPublishedEventCount());
    }

    @Test
    public void testNewImageDeployWithImageAndNoDeployment() {
        MockImageManager manager = new MockImageManager();
        manager.addImage(new Image("image1", "latest"));
        MockApplicationEventPublisher publisher = new MockApplicationEventPublisher();
        ImageDeployStatusListener listener = new ImageDeployStatusListener(manager, new MockDeploymentManager(), publisher);
        assertEquals(0, publisher.getPublishedEventCount());

        listener.onApplicationEvent(new ImageDeployStatusEvent(this, createImageDeploy(
                "default",
                "imagedeploy1",
                createDeploymentWithOneContainer(
                        "default",
                        "deploy1",
                        "container1",
                        encodeImageRef("image1"),
                        1
                )
        )
        ));

        assertEquals(1, publisher.getPublishedEventCount());
        assertTrue(publisher.getEvent(0) instanceof CreateDeploymentEvent);
        CreateDeploymentEvent event = (CreateDeploymentEvent) publisher.getEvent(0);
        assertEquals("default", event.getDeployment().getMetadata().getNamespace());
        assertEquals("deploy1", event.getDeployment().getMetadata().getName());
        assertEquals("latest", event.getDeployment().getSpec().getTemplate().getSpec().getContainers().get(0).getImage());
    }

    @Test
    public void testNewImageDeployWithImageAndExistingDeploymentWithOldImage() {
        MockImageManager imageManager = new MockImageManager();
        imageManager.addImage(new Image("image1", "latest"));

        MockDeploymentManager deployManager = new MockDeploymentManager();
        deployManager.addDeployment(createDeploymentWithOneContainer(
                "default",
                "deploy1",
                "container1",
                "old",
                1
        ));

        MockApplicationEventPublisher publisher = new MockApplicationEventPublisher();

        ImageDeployStatusListener listener = new ImageDeployStatusListener(imageManager, deployManager, publisher);
        assertEquals(0, publisher.getPublishedEventCount());
        listener.onApplicationEvent(new ImageDeployStatusEvent(this, createImageDeploy(
                "default",
                "imagedeploy1",
                createDeploymentWithOneContainer(
                        "default",
                        "deploy1",
                        "container1",
                        encodeImageRef("image1"),
                        1
                )
        )));

        assertEquals(1, publisher.getPublishedEventCount());
        assertTrue(publisher.getEvent(0) instanceof UpdateDeploymentEvent);
        UpdateDeploymentEvent event = (UpdateDeploymentEvent) publisher.getEvent(0);
        assertEquals("default", event.getDeployment().getMetadata().getNamespace());
        assertEquals("deploy1", event.getDeployment().getMetadata().getName());
    }

    @Test
    public void testNewImageDeployWithImageAndExistingDeploymentWithSameImage() {
        MockImageManager imageManager = new MockImageManager();
        imageManager.addImage(new Image("myimage", "latest"));

        MockDeploymentManager deployManager = new MockDeploymentManager();
        deployManager.addDeployment(createDeploymentWithOneContainer(
                "default",
                "mydeploy",
                "mycontainer",
                "latest",
                1)
        );

        MockApplicationEventPublisher publisher = new MockApplicationEventPublisher();

        ImageDeployStatusListener listener = new ImageDeployStatusListener(imageManager, deployManager, publisher);
        assertEquals(0, publisher.getPublishedEventCount());
        listener.onApplicationEvent(new ImageDeployStatusEvent(
                this,
                createImageDeploy(
                        "default",
                        "myimagedeploy",
                        createDeploymentWithOneContainer(
                                "default",
                                "mydeploy",
                                "myimage",
                                "latest",
                                1
                        )
                )
        ));
        assertEquals(0, publisher.getPublishedEventCount());
    }

    @Test
    public void testNewImageDeployWithImageAndExistingDeploymentWithTwoOldImages() {
        MockImageManager imageManager = new MockImageManager();
        imageManager.addImage(new Image("image1", "latest"));
        imageManager.addImage(new Image("image2", "latest"));

        MockDeploymentManager deployManager = new MockDeploymentManager();
        deployManager.addDeployment(createDeploymentWithTwoContainer(
                "default",
                "deploy1",
                "container1",
                "old1",
                "container2",
                "old2",
                1)
        );

        MockApplicationEventPublisher publisher = new MockApplicationEventPublisher();

        ImageDeployStatusListener listener = new ImageDeployStatusListener(imageManager, deployManager, publisher);
        assertEquals(0, publisher.getPublishedEventCount());
        listener.onApplicationEvent(new ImageDeployStatusEvent(this, createImageDeploy(
                "default",
                "imagedeploy1",
                createDeploymentWithTwoContainer(
                        "default",
                        "deploy1",
                        "container1",
                        encodeImageRef("image1"),
                        "container2",
                        encodeImageRef("image2"),
                        1
                )
                ))
        );

        assertEquals(1, publisher.getPublishedEventCount());
        assertTrue(publisher.getEvent(0) instanceof UpdateDeploymentEvent);
    }

    @Test
    public void testNewImageDeployWithUnmonitoredImagesAndExistingDeploymentWithTwoOldImages() {
        MockImageManager imageManager = new MockImageManager();
        imageManager.addImage(new Image("image1", "latest1"));

        MockDeploymentManager deployManager = new MockDeploymentManager();
        deployManager.addDeployment(createDeploymentWithTwoContainer(
                "default",
                "mydeploy",
                "mycontainer1",
                "latest1",
                "mycontainer2",
                "latest2",
                1)
        );

        MockApplicationEventPublisher publisher = new MockApplicationEventPublisher();

        ImageDeployStatusListener listener = new ImageDeployStatusListener(imageManager, deployManager, publisher);
        assertEquals(0, publisher.getPublishedEventCount());
        listener.onApplicationEvent(new ImageDeployStatusEvent(this, createImageDeploy(
                "default",
                "myimagedeploy",
                createDeploymentWithTwoContainer(
                        "default",
                        "mydeploy",
                        "mycontainer1",
                        encodeImageRef("image1"),
                        "mycontainer2",
                        "latest3",
                        1)
        )));

        assertEquals(1, publisher.getPublishedEventCount());
        assertTrue(publisher.getEvent(0) instanceof UpdateDeploymentEvent);
    }

}
