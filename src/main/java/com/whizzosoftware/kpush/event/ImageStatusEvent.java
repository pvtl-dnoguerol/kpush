package com.whizzosoftware.kpush.event;

import com.whizzosoftware.kpush.model.Image;
import org.springframework.context.ApplicationEvent;

public class ImageStatusEvent extends ApplicationEvent {
    private Image image;

    public ImageStatusEvent(Object source, Image image) {
        super(source);
        this.image = image;
    }

    public Image getImage() {
        return image;
    }

    @Override
    public String toString() {
        return "ImageUpdatedEvent(" + image + ")";
    }
}
