FROM public.ecr.aws/docker/library/node:24.13.1-slim AS build
RUN apt-get update \
 && apt-get install -y unzip curl ca-certificates openssl libsnappy-dev \
 && apt-get upgrade -y \
 && rm -rf /var/lib/apt/lists/*
WORKDIR /home/sunbird/telemetry
COPY ./telemetry-service.zip /home/sunbird/telemetry/
RUN unzip /home/sunbird/telemetry/telemetry-service.zip -d /home/sunbird/telemetry/
WORKDIR /home/sunbird/telemetry/telemetry
RUN npm prune --omit=dev

FROM public.ecr.aws/docker/library/node:24.13.1-slim
RUN npm install -g npm@11.10.0 \
 && npm pack tar@7.5.11 \
 && npm pack minimatch@10.2.1 \
 && npm pack picomatch@4.0.4 \
 && npm cache clean --force \
 && tar -xzf tar-7.5.11.tgz -C /usr/local/lib/node_modules/npm/node_modules/tar --strip-components=1 \
 && rm tar-7.5.11.tgz \
 && tar -xzf minimatch-10.2.1.tgz -C /usr/local/lib/node_modules/npm/node_modules/minimatch --strip-components=1 \
 && rm minimatch-10.2.1.tgz \
 && tar -xzf picomatch-4.0.4.tgz -C /usr/local/lib/node_modules/npm/node_modules/tinyglobby/node_modules/picomatch --strip-components=1 \
 && rm picomatch-4.0.4.tgz
RUN useradd -rm -d /home/sunbird -s /bin/bash -g root -G sudo -u 1001 sunbird
RUN apt-get update \
 && apt-get install -y curl ca-certificates openssl libsnappy-dev \
 && apt-get upgrade -y \
 && rm -rf /var/lib/apt/lists/*
USER sunbird
RUN mkdir -p /home/sunbird/telemetry
WORKDIR /home/sunbird/telemetry
COPY --from=build /home/sunbird/telemetry/telemetry/ /home/sunbird/telemetry/
CMD ["node", "app.js"]