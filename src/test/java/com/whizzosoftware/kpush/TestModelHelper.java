package com.whizzosoftware.kpush;

import com.whizzosoftware.kpush.model.ImageDeploy;
import io.kubernetes.client.models.V1Deployment;
import io.kubernetes.client.models.V1DeploymentBuilder;

public class TestModelHelper {
    static public V1Deployment createDeploymentWithOneContainer(String namespace, String name, String containerName, String containerImageName, int replicas) {
        return new V1DeploymentBuilder().
                withNewMetadata().
                    withNamespace(namespace).
                    withName(name).
                endMetadata().
                withNewSpec().
                    withReplicas(replicas).
                    withNewTemplate().
                        withNewSpec().
                            withContainers().
                                addNewContainer().
                                    withName(containerName).
                                    withImage(containerImageName).
                                endContainer().
                        endSpec().
                    endTemplate().
                endSpec().build();
    }

    static public V1Deployment createDeploymentWithTwoContainer(String namespace, String name, String container1Name, String containerImage1Name, String container2Name, String containerImage2Name, int replicas) {
        return new V1DeploymentBuilder().
                withNewMetadata().
                    withNamespace(namespace).
                    withName(name).
                endMetadata().
                withNewSpec().
                    withReplicas(replicas).
                    withNewTemplate().
                        withNewSpec().
                            withContainers().
                                addNewContainer().
                                    withName(container1Name).
                                    withImage(containerImage1Name).
                                endContainer().
                                addNewContainer().
                                    withName(container2Name).
                                    withImage(containerImage2Name).
                            endContainer().
                        endSpec().
                    endTemplate().
                endSpec().build();
    }

    static public ImageDeploy createImageDeploy(String namespace, String name, V1Deployment d) {
        return new ImageDeploy()
                .setMetadata(new ImageDeploy.Metadata()
                        .setName(name)
                        .setNamespace(namespace))
                .setSpec(new ImageDeploy.Spec().setDeployment(d)
        );
    }
}
