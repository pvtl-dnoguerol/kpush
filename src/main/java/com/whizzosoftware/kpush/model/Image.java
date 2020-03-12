package com.whizzosoftware.kpush.model;

public class Image {
    private String id;
    private String latest;

    public Image(String id, String latest) {
        this.id = id;
        this.latest = latest;
    }

    public String getId() {
        return id;
    }

    public String getLatest() {
        return latest;
    }

    @Override
    public String toString() {
        return "name=" + id + ",latest=" + latest;
    }
}
