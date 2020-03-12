package com.whizzosoftware.kpush.model;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ImageDeployList {
    private List<ImageDeploy> items;

    public ImageDeployList() {
    }

    public List<ImageDeploy> getItems() {
        return items;
    }

    public void setItems(List<ImageDeploy> items) {
        this.items = items;
    }
}
