FROM openjdk:8-jre-alpine
MAINTAINER "Manojv" "manojv@ilimi.in"
RUN apk update \
    && apk add  unzip \
    && apk add curl \
    && adduser -u 1001 -h /home/sunbird/ -D sunbird \
    && mkdir -p /home/sunbird/learner

COPY ./service/target/telemetry-service-1.0-SNAPSHOT-dist.zip /home/sunbird/learner/
RUN unzip /home/sunbird/learner/telemetry-service-1.0-SNAPSHOT-dist.zip -d /home/sunbird/learner/
RUN chown -R sunbird:sunbird /home/sunbird
USER sunbird
WORKDIR /home/sunbird/learner/
RUN mkdir -p /home/sunbird/learner/logs/
RUN touch /home/sunbird/learner/logs/telemetry_service_mw.log
RUN ln -sf /dev/stdout /home/sunbird/learner/logs/telemetry_service_mw.log
CMD java  -cp '/home/sunbird/learner/telemetry-service-1.0-SNAPSHOT/lib/*' Dhttp.port=9001 play.core.server.ProdServerStart  /home/sunbird/learner/telemetry-service-1.0-SNAPSHOT
