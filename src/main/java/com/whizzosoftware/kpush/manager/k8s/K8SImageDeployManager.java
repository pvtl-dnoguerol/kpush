package com.whizzosoftware.kpush.manager.k8s;

import com.whizzosoftware.kpush.manager.ImageDeployManager;
import com.whizzosoftware.kpush.model.ImageDeploy;
import com.whizzosoftware.kpush.model.ImageDeployList;
import io.kubernetes.client.ApiClient;
import io.kubernetes.client.ApiException;
import io.kubernetes.client.apis.CustomObjectsApi;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Component
public class K8SImageDeployManager implements ImageDeployManager {
    private final Logger logger = LoggerFactory.getLogger(K8SImageDeployManager.class);

    private static final String CRD_GROUP = "crd.whizzosoftware.com";
    private static final String CRD_VERSION = "v1alpha1";
    private static final String CRD_PLURAL = "imagedeploys";

    CustomObjectsApi coApiClient;

    public K8SImageDeployManager(ApiClient apiClient) {
        this.coApiClient = new CustomObjectsApi(apiClient);
    }

    @Override
    public Collection<ImageDeploy> getAllImageDeploys() {
        logger.trace("getAllImageDeploys()");
        try {
            Object o = coApiClient.listClusterCustomObject(CRD_GROUP, CRD_VERSION, CRD_PLURAL, "false", null, null, null, null, false);
            ImageDeployList list =  new ObjectMapper().convertValue(o, ImageDeployList.class);
            return list.getItems();
        } catch (ApiException e) {
            throw new RuntimeException("Error listing ImageDeploy resources", e);
        }
    }

    @Override
    public Collection<ImageDeploy> getImageDeploysForImageName(String imageName) {
        logger.trace("getImageDeploysForImageName(): " + imageName);
        List<ImageDeploy> results = new ArrayList<>();
        for (ImageDeploy id : getAllImageDeploys()) {
            if (id.hasImageReference(imageName)) {
                results.add(id);
            }
        }
        return results;
    }
}
