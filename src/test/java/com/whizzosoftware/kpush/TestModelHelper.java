package com.whizzosoftware.kpush;

import com.whizzosoftware.kpush.k8s.DeploymentHelper;
import com.whizzosoftware.kpush.model.ImageDeploy;
import io.kubernetes.client.models.V1Deployment;
import io.kubernetes.client.models.V1DeploymentBuilder;

import java.util.Collections;

public class TestModelHelper {
    static public V1Deployment createDeployment(String namespace, String name, String imageName, int replicas) {
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
                                    withImage(imageName).
                                endContainer().
                        endSpec().
                    endTemplate().
                endSpec().build();
    }

    static public ImageDeploy createImageDeploy(String namespace, String name, String deployName, String imageName, int replicas) {
        V1Deployment deployment = new V1DeploymentBuilder().
                withNewMetadata().
                    withNamespace(namespace).
                    withName(deployName).
                    withLabels(Collections.singletonMap("app", deployName)).
                endMetadata().
                withNewSpec().
                    withReplicas(replicas).
                    withNewSelector().
                        withMatchLabels(Collections.singletonMap("app", deployName)).
                    endSelector().
                    withNewTemplate().
                        withNewMetadata().
                            withLabels(Collections.singletonMap("app", deployName)).
                        endMetadata().
                        withNewSpec().
                            withContainers().
                                addNewContainer().
                                    withName(deployName).
                                    withImage(DeploymentHelper.encodeImageRef(imageName)).
                                endContainer().
                        endSpec().
                    endTemplate().
                endSpec().build();

        return new ImageDeploy().
                withNewMetadata().
                    withNamespace(namespace).
                    withName(name).
                endMetadata().
                withNewSpec().
                    withDeployment(deployment).
                endSpec();
    }
}
