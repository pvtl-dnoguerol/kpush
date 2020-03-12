package com.whizzosoftware.kpush.model;

import com.whizzosoftware.kpush.k8s.DeploymentHelper;
import io.kubernetes.client.models.V1Container;
import io.kubernetes.client.models.V1Deployment;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ImageDeploy {
    private Metadata metadata;
    private Spec spec;

    public ImageDeploy() {}

    public Metadata getMetadata() {
        return metadata;
    }

    public Spec getSpec() {
        return spec;
    }

    public boolean hasContainers() {
        return (spec != null && spec.hasContainers());
    }

    public boolean hasImageReference(String imageRef) {
        for (V1Container c : getSpec().getDeployment().getSpec().getTemplate().getSpec().getContainers()) {
            if (DeploymentHelper.encodeImageRef(imageRef).equals(c.getImage())) {
                return true;
            }
        }
        return false;
    }

    public List<V1Container> getContainersWithImageRef(String imageRef) {
        List<V1Container> results = new ArrayList<>();
        for (V1Container c : getSpec().getDeployment().getSpec().getTemplate().getSpec().getContainers()) {
            if (DeploymentHelper.encodeImageRef(imageRef).equals(c.getImage())) {
                results.add(c);
            }
        }
        return results;
    }

    public Metadata withNewMetadata() {
        this.metadata = new Metadata(this);
        return this.metadata;
    }

    public Spec withNewSpec() {
        this.spec = new Spec(this);
        return this.spec;
    }

    static public class Metadata {
        private ImageDeploy parent;
        private String namespace;
        private String name;
        private Map<String,String> labels;

        public Metadata(ImageDeploy parent) {
            this.parent = parent;
        }

        public String getNamespace() {
            return namespace;
        }

        public String getName() {
            return name;
        }

        public Map<String, String> getLabels() {
            return labels;
        }

        public Metadata withNamespace(String namespace) {
            this.namespace = namespace;
            return this;
        }

        public Metadata withName(String name) {
            this.name = name;
            return this;
        }

        public Metadata withLabels(Map<String,String> labels) {
            this.labels = labels;
            return this;
        }

        public ImageDeploy endMetadata() {
            return parent;
        }
    }

    static public class Spec {
        private ImageDeploy parent;
        private V1Deployment deployment;

        public Spec(ImageDeploy parent) {
            this.parent = parent;
        }

        public Spec withDeployment(V1Deployment deployment) {
            this.deployment = deployment;
            return this;
        }

        public V1Deployment getDeployment() {
            return deployment;
        }

        public boolean hasContainers() {
            return (deployment != null && deployment.getSpec().getTemplate().getSpec().getContainers().size() > 0);
        }

        public ImageDeploy endSpec() {
            return parent;
        }
    }
}
