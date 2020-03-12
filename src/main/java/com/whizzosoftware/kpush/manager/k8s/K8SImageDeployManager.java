package com.whizzosoftware.kpush.manager.k8s;

import com.whizzosoftware.kpush.manager.ImageDeployManager;
import com.whizzosoftware.kpush.model.ImageDeploy;
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
        List<ImageDeploy> results = new ArrayList<>();
        try {
            Object o = coApiClient.listClusterCustomObject(CRD_GROUP, CRD_VERSION, CRD_PLURAL, "false", null, null, null, null, false);
            Map map = (Map)o;
            List r = (List)map.get("items");
            for (Object id : r) {
                Map idmap = (Map)id;
                if ("ImageDeploy".equals(idmap.get("kind"))) {
                    Map metamap = (Map)idmap.get("metadata");
                    Map specmap = (Map)idmap.get("spec");
                    Map deployMap = (Map)specmap.get("deployment");

                    results.add(new ImageDeploy().
                            withNewMetadata().
                                withNamespace((String)metamap.get("namespace")).
                                withName((String)metamap.get("name")).
                            endMetadata().
                            withNewSpec().
                                withDeployment(null).
                            endSpec()
                    );
                }
            }
            return results;
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
