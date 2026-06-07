FROM gradle:8.10.2-jdk17 AS builder

USER root
RUN apt-get update \
 && apt-get install -y --no-install-recommends \
        libcurl4-openssl-dev \
        ca-certificates \
 && rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY . /app

RUN ARCH=$(uname -m) && \
    case "$ARCH" in \
        x86_64)            TASK=LinuxX64;   DIR=linuxX64   ;; \
        aarch64|arm64)     TASK=LinuxArm64; DIR=linuxArm64 ;; \
        *) echo "Unsupported arch: $ARCH" && exit 1 ;; \
    esac && \
    gradle ":apps:linuxApp:linkReleaseExecutable${TASK}" --no-daemon && \
    mv "apps/linuxApp/build/bin/${DIR}/releaseExecutable/linuxApp.kexe" /tmp/abkmyip

FROM ubuntu:24.04

RUN apt-get update \
 && apt-get install -y --no-install-recommends \
        libcurl4 \
        ca-certificates \
 && rm -rf /var/lib/apt/lists/*

COPY --from=builder /tmp/abkmyip /usr/local/bin/abkmyip

ENTRYPOINT ["/usr/local/bin/abkmyip"]
