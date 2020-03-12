package com.whizzosoftware.kpush.k8s;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ReplaceImageOpTest {
    @Test
    public void testConstructor() {
        ReplaceImageOp op = new ReplaceImageOp(0, "foo");
        assertEquals("replace", op.getOp());
        assertEquals("/spec/template/spec/containers/0/image", op.getPath());
        assertEquals("foo", op.getValue());
    }
}
