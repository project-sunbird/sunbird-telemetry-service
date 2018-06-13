FROM openjdk:8-jre-alpine
MAINTAINER "Manojv" "manojv@ilimi.in"
RUN apk update \
    && apk add  unzip \
    && apk add curl \
    && adduser -u 1001 -h /home/sunbird/ -D sunbird \
    && mkdir -p /home/sunbird/telemetry

COPY ./telemetry-servce/target/telemetry-service-1.0-SNAPSHOT-dist.zip /home/sunbird/telemetry/
RUN unzip /home/sunbird/telemetry/telemetry-service-1.0-SNAPSHOT-dist.zip -d /home/sunbird/telemetry/
RUN chown -R sunbird:sunbird /home/sunbird
USER sunbird
WORKDIR /home/sunbird/telemetry/
RUN mkdir -p /home/sunbird/telemetry/logs/
RUN touch /home/sunbird/telemetry/logs/telemetry_service_mw.log
RUN ln -sf /dev/stdout /home/sunbird/telemetry/logs/telemetry_service_mw.log
CMD java  -cp '/home/sunbird/telemetry/telemetry-service-1.0-SNAPSHOT/lib/*' -Dhttp.port=9001 play.core.server.ProdServerStart  /home/sunbird/telemetry/telemetry-service-1.0-SNAPSHOT
