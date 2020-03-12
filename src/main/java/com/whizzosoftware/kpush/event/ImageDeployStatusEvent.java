package com.whizzosoftware.kpush.event;

import com.whizzosoftware.kpush.model.ImageDeploy;
import org.springframework.context.ApplicationEvent;

public class ImageDeployStatusEvent extends ApplicationEvent {
    private ImageDeploy imageDeploy;

    public ImageDeployStatusEvent(Object source, ImageDeploy imageDeploy) {
        super(source);
        this.imageDeploy = imageDeploy;
    }

    public ImageDeploy getImageDeploy() {
        return imageDeploy;
    }

    @Override
    public String toString() {
        return "ImageDeploymentUpdateEvent(" + imageDeploy + ")";
    }
}
