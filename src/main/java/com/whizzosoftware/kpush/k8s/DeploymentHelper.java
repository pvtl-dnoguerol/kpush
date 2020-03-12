package com.whizzosoftware.kpush.k8s;

import io.kubernetes.client.models.V1Container;
import io.kubernetes.client.models.V1Deployment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class DeploymentHelper {
    static public Collection<String> getAllImageRefs(V1Deployment d) {
        List<String> results = new ArrayList<>();
        for (V1Container c : getAllContainers(d)) {
            if (isImageRef(c.getImage())) {
                results.add(DeploymentHelper.decodeImageRef(c.getImage()));
            }
        }
        return results;
    }

    static public Collection<V1Container> getAllContainers(V1Deployment d) {
        return d.getSpec().getTemplate().getSpec().getContainers();
    }

    static public void replaceAllImageRefs(V1Deployment d, Map<String,String> refToImageMap) {
        for (String ref : refToImageMap.keySet()) {
            String image = refToImageMap.get(ref);
            for (V1Container c : getContainersWithImageRef(d, ref)) {
                c.setImage(image);
            }
        }
    }

    static public List<V1Container> getContainersWithImageRef(V1Deployment d, String imageRef) {
        List<V1Container> results = new ArrayList<>();
        for (V1Container c : getAllContainers(d)) {
            if (DeploymentHelper.isImageRef(c.getImage()) && imageRef.equals(DeploymentHelper.decodeImageRef(c.getImage()))) {
                results.add(c);
            }
        }
        return results;
    }

    static public V1Container getContainerWithName(V1Deployment d, String name) {
        for (V1Container c : getAllContainers(d)) {
            if (name.equals(c.getName())) {
                return c;
            }
        }
        return null;
    }

    static public boolean isImageRef(String s) {
        return (s.startsWith("REF:"));
    }

    static public String encodeImageRef(String imageId) {
        return "REF:" + imageId;
    }

    static public String decodeImageRef(String imageRef) {
        if (isImageRef(imageRef)) {
            return imageRef.substring(4);
        } else {
            return imageRef;
        }
    }
}
