package com.whizzosoftware.kpush.manager;

import com.whizzosoftware.kpush.model.Image;

import java.util.Collection;

public interface ImageManager {
    Collection<Image> getAllImages();
    Collection<Image> getImages(Collection<String> ids);
    Image getImage(String imageName);
}
