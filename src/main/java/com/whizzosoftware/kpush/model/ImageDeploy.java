package com.whizzosoftware.kpush.model;

import com.whizzosoftware.kpush.k8s.DeploymentHelper;
import io.kubernetes.client.models.V1Container;
import io.kubernetes.client.models.V1Deployment;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class ImageDeploy {
    private Metadata metadata;
    private Spec spec;

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

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Accessors(chain = true)
    static public class Metadata {
        private ImageDeploy parent;
        private String namespace;
        private String name;
        private Map<String,String> labels;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Accessors(chain = true)
    static public class Spec {
        private ImageDeploy parent;
        private V1Deployment deployment;

        public boolean hasContainers() {
            return (deployment != null && deployment.getSpec().getTemplate().getSpec().getContainers().size() > 0);
        }
    }
}
