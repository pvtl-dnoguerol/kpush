package com.whizzosoftware.kpush.manager.k8s;

import com.whizzosoftware.kpush.k8s.DeploymentHelper;
import com.whizzosoftware.kpush.manager.DeploymentManager;
import io.kubernetes.client.ApiClient;
import io.kubernetes.client.ApiException;
import io.kubernetes.client.apis.AppsV1Api;
import io.kubernetes.client.custom.V1Patch;
import io.kubernetes.client.models.V1Deployment;
import io.kubernetes.client.models.V1DeploymentList;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class K8SDeploymentManager implements DeploymentManager {
    private final Logger logger = LoggerFactory.getLogger(K8SDeploymentManager.class);

    private AppsV1Api apiClient;

    public K8SDeploymentManager(ApiClient apiClient) {
        this.apiClient = new AppsV1Api(apiClient);
    }

    @Override
    public V1Deployment getDeployment(String namespace, String name) {
        try {
            V1DeploymentList list = apiClient.listNamespacedDeployment(namespace, null, null, null, null, null, null, null, null);
            for (V1Deployment d : list.getItems()) {
                if (name.equals(d.getMetadata().getName())) {
                    return d;
                }
            }
            return null;
        } catch (ApiException e) {
            throw new RuntimeException("Error retrieving deployments", e);
        }
    }

    @Override
    public void createDeployment(V1Deployment d) {
        logger.debug("Attempting to create deployment: {}", d);

        // create deployment
        try {
            apiClient.createNamespacedDeployment(d.getMetadata().getNamespace(), d, null, null, null);
            logger.info("Created new deployment: {}", d.getMetadata().getName());
        } catch (ApiException e) {
            throw new RuntimeException("Error creating new deployment", e);
        }
    }

    @Override
    public void updateDeployment(V1Deployment d) {
        logger.debug("Attempting to update deployment: {}, {}", d.getMetadata().getNamespace(), d.getMetadata().getName());

        // patch the deployment with new container images
        try {
            V1Patch patch = new V1Patch(new ObjectMapper().writeValueAsString(DeploymentHelper.createReplaceImageOps(d)));
            V1Deployment d2 = apiClient.patchNamespacedDeployment(d.getMetadata().getName(), d.getMetadata().getNamespace(), patch, null, null, null, null);
            logger.info("Patched deployment: {}", d2.getMetadata().getName());
        } catch (Exception e) {
            logger.error("Error updating deployment", e);
        }
    }
}
