ARG DHI_IMAGE_DEV=dhi.io/node:24.17.0-debian13-dev
ARG DHI_IMAGE_RUNTIME=dhi.io/node:24.17.0-debian13

FROM dhi.io/busybox:1.38.0-alpine3.24 AS shell

# ---- Build stage (has npm + apt) ----
FROM ${DHI_IMAGE_DEV} AS build

RUN apt-get update \
    && apt-get install -y unzip curl ca-certificates openssl libsnappy-dev gzip \
    # && apt-get upgrade -y \
    && rm -rf /var/lib/apt/lists/*

# Optional: add custom/internal CA certs. Uncomment both lines and drop your
# .crt files into a local "custom-certs/" folder next to this Dockerfile to enable.
# COPY custom-certs/*.crt /usr/local/share/ca-certificates/
# RUN update-ca-certificates

# Patch npm's bundled dependencies
RUN npm install -g npm@11.10.0 --force \
    && npm pack tar@7.5.19 \
    && npm pack minimatch@10.2.1 \
    && npm pack picomatch@4.0.4 \
    && npm pack brace-expansion@5.0.7 \
    && npm pack balanced-match@4.0.4 \
    && npm cache clean --force \
    && tar -xzf tar-7.5.19.tgz -C /usr/lib/node_modules/npm/node_modules/tar --strip-components=1 \
    && rm tar-7.5.19.tgz \
    && tar -xzf minimatch-10.2.1.tgz -C /usr/lib/node_modules/npm/node_modules/minimatch --strip-components=1 \
    && rm minimatch-10.2.1.tgz \
    && tar -xzf picomatch-4.0.4.tgz -C /usr/lib/node_modules/npm/node_modules/tinyglobby/node_modules/picomatch --strip-components=1 \
    && rm picomatch-4.0.4.tgz \
    # dependency for minimatch
    && mkdir -p /usr/lib/node_modules/npm/node_modules/minimatch/node_modules/brace-expansion \
    && tar -xzf brace-expansion-5.0.7.tgz -C /usr/lib/node_modules/npm/node_modules/minimatch/node_modules/brace-expansion --strip-components=1 \
    && rm brace-expansion-5.0.7.tgz \
    # dependency for brace-expansion
    && mkdir -p /usr/lib/node_modules/npm/node_modules/minimatch/node_modules/brace-expansion/node_modules/balanced-match \
    && tar -xzf balanced-match-4.0.4.tgz -C /usr/lib/node_modules/npm/node_modules/minimatch/node_modules/brace-expansion/node_modules/balanced-match --strip-components=1 \
    && rm balanced-match-4.0.4.tgz


# Extract and prune the app
WORKDIR /home/sunbird/telemetry
COPY ./telemetry-service.zip .
RUN unzip telemetry-service.zip -d . && rm telemetry-service.zip
WORKDIR /home/sunbird/telemetry/telemetry
RUN npm prune --omit=dev

# ---- Runtime stage (minimal) ----
FROM ${DHI_IMAGE_RUNTIME}
COPY --from=shell /lib/ld-musl-x86_64.so.1 /lib/ld-musl-x86_64.so.1
COPY --from=shell /bin/busybox /bin/sh

# Copy patched npm (already patched in build stage)
COPY --from=build /usr/lib/node_modules/npm /usr/lib/node_modules/npm
COPY --from=build /usr/bin/npm /usr/bin/npm
COPY --from=build /usr/bin/npx /usr/bin/npx

# Copy runtime OS packages (libsnappy, curl, openssl, ca-certs)
COPY --from=build /usr/lib/x86_64-linux-gnu/libsnappy* /usr/lib/x86_64-linux-gnu/
COPY --from=build /usr/bin/curl /usr/bin/curl
COPY --from=build /lib/x86_64-linux-gnu/libssl* /lib/x86_64-linux-gnu/
COPY --from=build /lib/x86_64-linux-gnu/libcrypto* /lib/x86_64-linux-gnu/
COPY --from=build /etc/ssl /etc/ssl
# Only needed if the custom CA certs block above (build stage) is enabled:
# COPY --from=build /usr/local/share/ca-certificates /usr/local/share/ca-certificates

# Copy the app
USER node
COPY --from=build --chown=nonroot:nonroot /home/sunbird/telemetry/telemetry /home/sunbird/telemetry/
WORKDIR /home/sunbird/telemetry/

CMD ["node", "app.js"]