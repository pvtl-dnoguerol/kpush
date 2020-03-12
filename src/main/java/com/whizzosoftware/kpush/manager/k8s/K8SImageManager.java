package com.whizzosoftware.kpush.manager.k8s;

import com.whizzosoftware.kpush.manager.ImageManager;
import com.whizzosoftware.kpush.model.Image;
import io.kubernetes.client.ApiClient;
import io.kubernetes.client.ApiException;
import io.kubernetes.client.apis.CustomObjectsApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Component
public class K8SImageManager implements ImageManager {
    private final Logger logger = LoggerFactory.getLogger(K8SImageManager.class);

    private static final String CRD_GROUP = "build.pivotal.io";
    private static final String CRD_VERSION = "v1alpha1";
    private static final String CRD_PLURAL = "images";

    private CustomObjectsApi api;

    public K8SImageManager(ApiClient apiClient) {
        this.api = new CustomObjectsApi(apiClient);
    }

    @Override
    public List<Image> getAllImages() {
        logger.trace("getAllImages()");

        try {
            List<Image> results = new ArrayList<>();
            Object o = api.listClusterCustomObject(CRD_GROUP, CRD_VERSION, CRD_PLURAL, "false", null, null, null, null, false);
            Map map = (Map)o;
            List r = (List)map.get("items");
            for (Object id : r) {
                Map idmap = (Map)id;
                if ("Image".equals(idmap.get("kind"))) {
                    Map metamap = (Map)idmap.get("metadata");
                    Map statusmap = (Map)idmap.get("status");
                    results.add(new Image((String)metamap.get("name"), (String)statusmap.get("latestImage")));
                }
            }
            return results;
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
            if (imageName.equals(i.getId())) {
                return i;
            }
        }

        return null;
    }
}
