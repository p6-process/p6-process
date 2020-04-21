FROM registry.access.redhat.com/ubi8/ubi-minimal:8.1-409
WORKDIR /work/
COPY target/*-runner /work/application
RUN mkdir -p /work/p6 && chmod -R 775 /work

EXPOSE 8080
CMD ["./application", "-Dquarkus.http.host=0.0.0.0"]