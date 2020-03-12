package com.whizzosoftware.kpush.manager;

import com.whizzosoftware.kpush.model.ImageDeploy;

import java.util.Collection;

public interface ImageDeployManager {
    Collection<ImageDeploy> getAllImageDeploys();
    Collection<ImageDeploy> getImageDeploysForImageName(String imageName);
}
