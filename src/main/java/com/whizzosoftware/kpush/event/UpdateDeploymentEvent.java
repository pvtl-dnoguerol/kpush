package com.whizzosoftware.kpush.event;

import io.kubernetes.client.models.V1Deployment;
import org.springframework.context.ApplicationEvent;

public class UpdateDeploymentEvent extends ApplicationEvent {
    private V1Deployment deployment;

    public UpdateDeploymentEvent(Object source, V1Deployment deployment) {
        super(source);
        this.deployment = deployment;
    }

    public V1Deployment getDeployment() {
        return deployment;
    }
}
