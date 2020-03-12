package com.whizzosoftware.kpush.k8s;

import lombok.Data;

@Data
public class ReplaceImageOp {
    private String op = "replace";
    private final String path;
    private final String value;

    public ReplaceImageOp(int index, String image) {
        this.path = "/spec/template/spec/containers/" + index + "/image";
        this.value = image;
    }
}
