package com.whizzosoftware.kpush.model;

import io.kubernetes.client.models.V1Deployment;
import io.kubernetes.client.models.V1DeploymentBuilder;
import io.kubernetes.client.models.V1EnvVarBuilder;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

public class ImageDeployTest {
    @Test
    public void testFluentConstructors() {
        ImageDeploy deploy = createImageDeployWithContainers();
        assertEquals("my-image-deploy", deploy.getMetadata().getName());
        assertEquals("myapp", deploy.getMetadata().getLabels().get("app"));
        assertEquals("mydeploy", deploy.getSpec().getDeployment().getMetadata().getName());
        assertEquals("mynamespace", deploy.getSpec().getDeployment().getMetadata().getNamespace());
        assertEquals("mydeployapp", deploy.getSpec().getDeployment().getMetadata().getLabels().get("app"));
        assertEquals(2, deploy.getSpec().getDeployment().getSpec().getReplicas());
        assertEquals("matchapp", deploy.getSpec().getDeployment().getSpec().getSelector().getMatchLabels().get("app"));
        assertEquals("mydeployapp2", deploy.getSpec().getDeployment().getSpec().getTemplate().getMetadata().getLabels().get("app"));
        assertEquals("container1", deploy.getSpec().getDeployment().getSpec().getTemplate().getSpec().getContainers().get(0).getName());
        assertEquals("image1", deploy.getSpec().getDeployment().getSpec().getTemplate().getSpec().getContainers().get(0).getImage());
        assertEquals("foo", deploy.getSpec().getDeployment().getSpec().getTemplate().getSpec().getContainers().get(0).getEnv().get(0).getName());
        assertEquals("bar", deploy.getSpec().getDeployment().getSpec().getTemplate().getSpec().getContainers().get(0).getEnv().get(0).getValue());
        assertFalse(deploy.hasImageReference("mycontainer"));
        assertTrue(deploy.hasImageReference("extimage2"));
    }

    @Test
    public void testHasContainers() {
        ImageDeploy deploy = createImageDeployWithoutContainers();
        assertFalse(deploy.hasContainers());
    }

    private ImageDeploy createImageDeployWithContainers() {
        V1Deployment deployment = new V1DeploymentBuilder().
                withNewMetadata().
                    withName("mydeploy").
                    withNamespace("mynamespace").
                    withLabels(Collections.singletonMap("app", "mydeployapp")).
                endMetadata().
                withNewSpec().
                    withReplicas(2).
                    withNewSelector().
                        withMatchLabels(Collections.singletonMap("app", "matchapp")).
                    endSelector().
                    withNewTemplate().
                        withNewMetadata().
                            withLabels(Collections.singletonMap("app", "mydeployapp2")).
                        endMetadata().
                        withNewSpec().
                            withContainers().
                                addNewContainer().
                                    withName("container1").
                                    withImage("image1").
                                    withEnv(Collections.singletonList(new V1EnvVarBuilder().withName("foo").withValue("bar").build())).
                                endContainer().
                                addNewContainer().
                                    withName("container2").
                                    withImage("REF:extimage2").
                                endContainer().
                        endSpec().
                    endTemplate().
                endSpec().build();

        return new ImageDeploy().
                withNewMetadata().
                withName("my-image-deploy").
                withLabels(Collections.singletonMap("app", "myapp")).
                endMetadata().
                withNewSpec().
                withDeployment(deployment).
                endSpec();
    }

    private ImageDeploy createImageDeployWithoutContainers() {
        V1Deployment deployment = new V1DeploymentBuilder().
                withNewMetadata().
                withName("mydeploy").
                withNamespace("mynamespace").
                withLabels(Collections.singletonMap("app", "mydeployapp")).
                endMetadata().
                withNewSpec().
                withReplicas(2).
                withNewSelector().
                withMatchLabels(Collections.singletonMap("app", "matchapp")).
                endSelector().
                withNewTemplate().
                withNewMetadata().
                withLabels(Collections.singletonMap("app", "mydeployapp2")).
                endMetadata().
                withNewSpec().
                endSpec().
                endTemplate().
                endSpec().build();

        return new ImageDeploy().
                withNewMetadata().
                withName("my-image-deploy").
                withLabels(Collections.singletonMap("app", "myapp")).
                endMetadata().
                withNewSpec().
                withDeployment(deployment).
                endSpec();
    }
}
