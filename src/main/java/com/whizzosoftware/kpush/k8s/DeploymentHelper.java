package com.whizzosoftware.kpush.k8s;

import io.kubernetes.client.models.V1Container;
import io.kubernetes.client.models.V1Deployment;

import java.util.*;
import java.util.stream.Collectors;

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
        if (d != null && d.getSpec() != null && d.getSpec().getTemplate() != null) {
            return d.getSpec().getTemplate().getSpec().getContainers();
        } else {
            return Collections.emptyList();
        }
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

    static public Collection<ReplaceImageOp> createReplaceImageOps(V1Deployment d) {
        List<V1Container> containers = d.getSpec().getTemplate().getSpec().getContainers();
        List<ReplaceImageOp> results = new ArrayList<>();
        for (int i=0; i < containers.size(); i++) {
            results.add(new ReplaceImageOp(i, containers.get(i).getImage()));
        }
        return results;
    }

    static public boolean isImageRef(String s) {
        return (s.startsWith("REF="));
    }

    static public String encodeImageRef(String imageId) {
        return "REF=" + imageId;
    }

    static public String decodeImageRef(String imageRef) {
        return isImageRef(imageRef) ? imageRef.substring(4) : imageRef;
    }
}
