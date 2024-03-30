#Dockerfile used for Pipeline builds
ARG MANDREL_IMAGE=quay.io/quarkus/ubi-quarkus-mandrel-builder-image:jdk-21

FROM ${MANDREL_IMAGE}

USER root

RUN microdnf --setopt=install_weak_deps=0 --setopt=tsflags=nodocs install -y bash curl findutils \
    && microdnf clean all && [ ! -d /var/cache/yum ] || rm -rf /var/cache/yum

ENTRYPOINT []
