package com.whizzosoftware.kpush.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Image {
    private Metadata metadata;
    private Status status;

    public Image(String name, String latest) {
        metadata = new Metadata(null, name, null);
        status = new Status(latest);
    }

    public String getName() {
        return metadata.getName();
    }

    public String getLatest() {
        return status.getLatestImage();
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Accessors(chain = true)
    @JsonIgnoreProperties(ignoreUnknown = true)
    static public class Metadata {
        private String namespace;
        private String name;
        private Map<String,String> labels;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Accessors(chain = true)
    @JsonIgnoreProperties(ignoreUnknown = true)
    static public class Status {
        private String latestImage;
    }
}
