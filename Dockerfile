FROM node:22.15-slim
MAINTAINER "Mahesh" "mahesh@ilimi.in"
RUN useradd -rm -d /home/sunbird -s /bin/bash -g root -G sudo -u 1001 sunbird
RUN apt-get update \
 && apt-get install -y unzip curl ca-certificates openssl libsnappy-dev \
 && rm -rf /var/lib/apt/lists/*
USER sunbird
RUN mkdir -p /home/sunbird/telemetry
WORKDIR /home/sunbird/telemetry
COPY ./telemetry-service.zip /home/sunbird/telemetry/
RUN unzip /home/sunbird/telemetry/telemetry-service.zip
RUN ls -all /home/sunbird/telemetry
WORKDIR /home/sunbird/telemetry/telemetry/
CMD ["node", "app.js"]