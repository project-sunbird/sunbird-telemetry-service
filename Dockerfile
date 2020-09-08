FROM node:14.9-buster-slim
MAINTAINER "Mahesh" "mahesh@ilimi.in"
RUN useradd -rm -d /home/sunbird -s /bin/bash -g root -G sudo -u 1001 sunbird
RUN apt-get update
RUN apt-get install unzip -y \
    && apt-get install curl -y \
    && apt-get install ca-certificates openssl -y
USER sunbird
RUN mkdir -p /home/sunbird/telemetry
WORKDIR /home/sunbird/telemetry
COPY ./telemetry-service.zip  /home/sunbird/telemetry/
RUN unzip /home/sunbird/telemetry/telemetry-service.zip
RUN ls -all /home/sunbird/telemetry
WORKDIR /home/sunbird/telemetry/telemetry/
CMD ["node", "app.js", "&"]