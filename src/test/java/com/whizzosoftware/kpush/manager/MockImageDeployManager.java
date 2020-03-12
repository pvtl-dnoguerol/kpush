package com.whizzosoftware.kpush.manager;

import com.whizzosoftware.kpush.k8s.DeploymentHelper;
import com.whizzosoftware.kpush.model.ImageDeploy;
import io.kubernetes.client.models.V1Container;

import java.util.*;

public class MockImageDeployManager implements ImageDeployManager {
    private Map<String,List<ImageDeploy>> deliveryMap = new HashMap<>();

    @Override
    public Collection<ImageDeploy> getAllImageDeploys() {
        return Collections.emptyList(); // TODO
    }

    @Override
    public Collection<ImageDeploy> getImageDeploysForImageName(String imageName) {
        return deliveryMap.computeIfAbsent(imageName, k -> new ArrayList<>());
    }

    public void addImageDeploy(ImageDeploy imageDeploy) {
        for (V1Container c : imageDeploy.getSpec().getDeployment().getSpec().getTemplate().getSpec().getContainers()) {
            List<ImageDeploy> deliveries = deliveryMap.computeIfAbsent(DeploymentHelper.decodeImageRef(c.getImage()), k -> new ArrayList<>());
            deliveries.add(imageDeploy);
        }
    }
}
