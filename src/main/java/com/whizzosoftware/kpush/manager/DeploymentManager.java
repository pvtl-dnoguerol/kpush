package com.whizzosoftware.kpush.manager;

import io.kubernetes.client.models.V1Deployment;

public interface DeploymentManager {
    V1Deployment getDeployment(String namespace, String name);
    void createDeployment(V1Deployment d);
    void updateDeployment(V1Deployment d);
}
