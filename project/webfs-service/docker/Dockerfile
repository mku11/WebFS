FROM openjdk:latest
WORKDIR /opt/webfs
ADD webfs-service-2.2.0.war .
ADD keystore.p12 .
ADD start-webfs-service.sh .
ADD config/application.properties config/application.properties
RUN mkdir -p fs
ENTRYPOINT ["/bin/sh","/opt/webfs/start-webfs-service.sh"]
