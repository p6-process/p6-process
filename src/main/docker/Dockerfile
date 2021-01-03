FROM registry.access.redhat.com/ubi8/ubi-minimal:8.3
WORKDIR /work/
COPY target/*-runner /work/application
RUN mkdir -p /work/p6 && chmod -R 775 /work

LABEL org.opencontainers.image.source https://github.com/p6-process/p6-process

EXPOSE 8080
CMD ["./application", "-Dquarkus.http.host=0.0.0.0"]