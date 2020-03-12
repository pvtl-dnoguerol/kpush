package com.whizzosoftware.kpush.k8s;

import static com.whizzosoftware.kpush.TestModelHelper.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Iterator;

public class DeploymentHelperTest {
    @Test
    public void testCreateReplaceImageOps() {
        Collection<ReplaceImageOp> ops = DeploymentHelper.createReplaceImageOps(createDeploymentWithTwoContainer(
           "default",
           "deploy1",
           "container1",
           "foo",
           "container2",
           "bar",
           1
        ));

        assertEquals(2, ops.size());
        Iterator<ReplaceImageOp> it = ops.iterator();
        ReplaceImageOp op = it.next();
        assertEquals("/spec/template/spec/containers/0/image", op.getPath());
        assertEquals("foo", op.getValue());
        op = it.next();
        assertEquals("/spec/template/spec/containers/1/image", op.getPath());
        assertEquals("bar", op.getValue());
    }
}
