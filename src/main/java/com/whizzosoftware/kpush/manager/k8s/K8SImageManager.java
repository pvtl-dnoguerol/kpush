package com.whizzosoftware.kpush.manager.k8s;

import com.whizzosoftware.kpush.manager.ImageManager;
import com.whizzosoftware.kpush.model.Image;
import com.whizzosoftware.kpush.model.ImageList;
import io.kubernetes.client.ApiClient;
import io.kubernetes.client.ApiException;
import io.kubernetes.client.apis.CustomObjectsApi;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Component
public class K8SImageManager implements ImageManager {
    private final Logger logger = LoggerFactory.getLogger(K8SImageManager.class);

    @Value("${IMAGE_CRD_GROUP:build.pivotal.io}")
    private String CRD_GROUP;
    @Value("${IMAGE_CRD_VERSION:v1alpha1}")
    private String CRD_VERSION;
    @Value("${IMAGE_CRD_PLURAL:images}")
    private String CRD_PLURAL;

    private CustomObjectsApi api;

    public K8SImageManager(ApiClient apiClient) {
        this.api = new CustomObjectsApi(apiClient);
    }

    @Override
    public List<Image> getAllImages() {
        logger.trace("getAllImages()");

        try {
            Object o = api.listClusterCustomObject(CRD_GROUP, CRD_VERSION, CRD_PLURAL, "false", null, null, null, null, false);
            ImageList list = new ObjectMapper().convertValue(o, ImageList.class);
            return list.getItems();
        } catch (ApiException e) {
            throw new RuntimeException("Error retrieving list of Image resources", e);
        }
    }

    @Override
    public Collection<Image> getImages(Collection<String> ids) {
        List<Image> results = new ArrayList<>();
        for (String id : ids) {
            Image i = getImage(id);
            if (i != null) {
                results.add(i);
            }
        }
        return results;
    }

    @Override
    public Image getImage(String imageName) {
        logger.trace("getImage(): {}", imageName);

        for (Image i : getAllImages()) {
            if (imageName.equals(i.getName())) {
                return i;
            }
        }

        return null;
    }
}
