package com.whizzosoftware.kpush.manager;

import io.kubernetes.client.models.V1Deployment;

import java.util.ArrayList;
import java.util.List;

public class MockDeploymentManager implements DeploymentManager {
    private List<V1Deployment> deployments = new ArrayList<>();

    @Override
    public V1Deployment getDeployment(String namespace, String name) {
        return deployments.stream().filter(d -> d.getMetadata().getNamespace().equals(namespace) && d.getMetadata().getName().equals(name)).findAny().orElse(null);
    }

    @Override
    public void createDeployment(V1Deployment d) {
        addDeployment(d);
    }

    @Override
    public void updateDeployment(V1Deployment d) {
        addDeployment(d);
    }

    public int getDeploymentCount() {
        return deployments.size();
    }

    public V1Deployment getDeployment(int ix) {
        return deployments.get(ix);
    }

    public void addDeployment(V1Deployment d) {
        deployments.add(d);
    }
}
