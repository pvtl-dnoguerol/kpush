package com.whizzosoftware.kpush.manager;

import com.whizzosoftware.kpush.model.Image;

import java.util.*;

public class MockImageManager implements ImageManager {
    private Map<String, Image> images = new HashMap<>();

    @Override
    public Collection<Image> getAllImages() {
        return images.values();
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
        return images.get(imageName);
    }

    public void addImage(Image image) {
        images.put(image.getId(), image);
    }
}
