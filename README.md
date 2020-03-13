# kpush

This project explores applying the Kubernetes controller reconciliation model to continuous delivery.

*kpush* monitors three things:

* `Image` resources representing an image in a container image registry. This includes a status indicating the latest available version of that image.
* `ImageDeploy` resources that references an `Image` resource and has an embedded deployment specification.
* Standard K8S `Deployment` resources based on the `ImageDeploy`'s deployment specification.

*kpush*'s job then becomes to watch for new/updated `Image` and `ImageDeploy` resources and will create/update the relevant `Deployment` resources. For updates, it will patch the existing `Deployment` so the declared deployment strategy (such as rolling update) is respected.   

This approach happens to work quite nicely with the [kpack](https://github.com/pivotal/kpack) container build service which updates `Image` resources as it performs its builds. A simple use case for *kpush* is to run in the same dev/test cluster as *kpack* and automatically deploying what *kpack* builds.
By default, *kpush* watches *kpack* `Image` resources based on the *kpack* CRD but it can be configured to monitor different CRDs using `IMAGE_CRD_GROUP`, `IMAGE_CRD_VERSION` and `IMAGE_CRD_PLURAL` environment variables.

But this approach could also facilitate use of an externalized entity that reconciles `Image` resources across multiple environment-specific clusters based on the knowledge of what container images have been promoted to what environments. In such a context, *kpush* is a small but important component.

![kpush flow](/img/flow.png)

## Usage

The `release.yml` file in the `k8s` directory contains the CRD for `ImageDeploy` as well as example `ClusterRole`, `ClusterRoleBinding`, `ServiceAccount` and `Deployment` resources needed to run *kpush*.

    kubectl create ns kpush
    kubectl apply -f release.yml

Once it is running, you will need to create `ImageResource` resources in order to do anything useful. Here is an example:

    apiVersion: crd.whizzosoftware.com/v1alpha1
    kind: ImageDeploy
    metadata:
      name: my-image-deploy
      namespace: default
    spec:
      deployment:
        metadata:
          name: my-app
          namespace: default
          labels:
            app: my-app
        spec:
          replicas: 1
          selector:
            matchLabels:
              app: my-app
          template:
            metadata:
              labels:
                app: my-app
            spec:
              containers:
                - name: my-app
                  image: REF=imagename

Note the container definition uses a `REF=` prefix for the image name. The name references the name of an `Image` resource which will be looked up to determine the latest available container image. The reference will be replaced with the actual container image in the resulting `Deployment` resource change. In the case of *kpack*, this would be the name of the `Image` resource created to support builds. Multiple containers with `Image` references are supported. 

## TODO

* The controller currently polls for `Image` and `ImageDeploy` resources. This is woefully inefficient and needs to be refactored to a watch. 
