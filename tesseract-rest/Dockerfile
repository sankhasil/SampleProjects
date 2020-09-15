FROM ubuntu:18.04 AS artifactory

ARG ARTIFACTORY_USER
ARG ARTIFACTORY_PASSWORD
ARG BASE_URL=https://archive.apache.org/dist
ARG JAR_VERSION=1.0-SNAPSHOT
ENV TESSDATA_PREFIX=/opt/tesseractOCR/tessdata_best

RUN apt-get update && apt-get install -y --no-install-recommends \
    ca-certificates \
    curl \
    wget

RUN mkdir -p /root/tesseract-rest

RUN curl -fSL https://${ARTIFACTORY_USER}:${ARTIFACTORY_PASSWORD}@download.ai4bd.org/tesseract-rest/tesseractOCR-REST-${JAR_VERSION}.jar -o /root/tesseract-rest/tesseractOCR-REST-${JAR_VERSION}.jar 


FROM ubuntu:18.04

ARG JAR_VERSION=1.0-SNAPSHOT

#Java Installation
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
    git \
    libtesseract4 \
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

RUN set -ex; \
	\
# deal with slim variants not having man page directories (which causes "update-alternatives" to fail)
	if [ ! -d /usr/share/man/man1 ]; then \
		mkdir -p /usr/share/man/man1; \
	fi; \
	\
	apt-get update; \
	apt-get install -y --no-install-recommends \
		openjdk-8-jdk \
	; \
	\
# verify that "docker-java-home" returns what we expect
	[ "$(readlink -f "$JAVA_HOME")" = "$(docker-java-home)" ]; \
	\
# update-alternatives so that future installs of other OpenJDK versions don't change /usr/bin/java
    update-alternatives --get-selections | awk -v home="$(readlink -f "$JAVA_HOME")" 'index($3, home) == 1 { $2 = "manual"; print | "update-alternatives --set-selections" }'; \
# ... and verify that it actually worked for one of the alternatives we care about
    update-alternatives --query java | grep -q 'Status: manual'


#Need to write the docker separately 
    
COPY --from=artifactory /root/tesseract-rest/tesseractOCR-REST-${JAR_VERSION}.jar /opt/tesseractOCR/
COPY tesseractOCR-REST-entrypoint.sh /usr/local/bin/
COPY goss.yaml /opt/tesseractOCR/

ENV TESSDATA_PREFIX /opt/tesseractOCR/tessdata_best
RUN cd /opt/tesseractOCR && git clone https://github.com/tesseract-ocr/tessdata_best.git && chmod +x /usr/local/bin/tesseractOCR-REST-entrypoint.sh && curl -fsSL https://goss.rocks/install | sh && chmod +rx /usr/local/bin/goss

    
ENTRYPOINT ["tesseractOCR-REST-entrypoint.sh"]

