FROM maven:3-openjdk-11 AS artifactory

ADD ./ /root/image-extraction-service
WORKDIR /root/image-extraction-service
RUN mvn clean package


FROM openjdk:11-jdk-slim

RUN apt-get update && apt-get install -y --no-install-recommends \
    ca-certificates \
    curl \
    netbase \
    netcat \
    wget \
    bzip2 \
    unzip \
    xz-utils \
    dialog \
    apt-utils \
	&& set -ex; \
    if ! command -v gpg > /dev/null; then \
    apt-get install -y --no-install-recommends \
    gnupg \
    dirmngr \
    ; \
    fi

# Default to UTF-8 file.encoding
ENV LANG C.UTF-8

# add a simple script that can auto-detect the appropriate JAVA_HOME value
# based on whether the JDK or only the JRE is installed
RUN { \
		echo '#!/bin/sh'; \
		echo 'set -e'; \
		echo; \
		echo 'dirname "$(dirname "$(readlink -f "$(which javac || which java)")")"'; \
	} > /usr/local/bin/docker-java-home \
	&& chmod +x /usr/local/bin/docker-java-home

# do some fancy footwork to create a JAVA_HOME that's cross-architecture-safe
RUN ln -svT "/usr/lib/jvm/java-8-openjdk-$(dpkg --print-architecture)" /docker-java-home
ENV JAVA_HOME /docker-java-home

COPY --from=artifactory /root/image-extraction-service/target/image-extraction-*.jar /opt/image-extraction-rest/
COPY image-extraction-entrypoint.sh /usr/local/bin/
COPY goss.yaml /opt/image-extraction-rest/

RUN cd /opt/image-extraction-rest && \
    chmod +x /usr/local/bin/image-extraction-entrypoint.sh && \
    curl -fsSL https://goss.rocks/install | sh && \
    chmod +rx /usr/local/bin/goss

ENTRYPOINT ["image-extraction-entrypoint.sh"]

