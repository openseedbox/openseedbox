# syntax=docker/dockerfile:1

FROM openseedbox/base as base


FROM base as builder

RUN apt update -qq && apt install -y --no-install-recommends \
	git ca-certificates python;

RUN bash -c "for repo in openseedbox{-common,}; do echo updating git for \$repo; cd /src/\$repo; git pull --ff-only origin master; done"
RUN bash -c "for repo in openseedbox{-common,}; do echo updating play deps for \$repo; cd /src/\$repo; /play/play deps --sync --forProd; done"

COPY application.conf /src/openseedbox/conf/application.conf
RUN touch /src/openseedbox/conf/application.runtime.conf

WORKDIR /src
RUN /play/play war openseedbox -o war
# Remove old Play! bundled jars
RUN bash -c "rm -fv war/WEB-INF/lib/{postgresql-9,log4j-1,slf4j-log4j12-1}.*.jar;"

# Remove irrelevant application directories
RUN bash -c "rm -rfv war/WEB-INF/application/{docker,test,javadoc,lib}"

# (Optional) Suppress log messages "At least one JAR was scanned for TLDs yet contained no TLDs." during Tomcat startup
RUN mkdir war/META-INF; \
	echo '<?xml version="1.0" encoding="UTF-8"?>\n\
<Context>\n\
    <JarScanner>\n\
        <JarScanFilter tldSkip="*.*"/>\n\
    </JarScanner>\n\
</Context>' > war/META-INF/context.xml;
# Only necessary if using Tomcat >= 10.0 due to package name change from javax.* to jakarta.*
RUN bash -c "mv -v war/WEB-INF/application/precompiled/java/play war/WEB-INF/classes"


FROM builder as tester
# Autotest OpenSeedbox
WORKDIR /src/openseedbox
# sync again but without --forProd to include openseedbox-common tests
RUN /play/play deps --sync
RUN RANDOM_HTTP_PORT=$(shuf -i 10000-59152 -n 1); sed -i -e 's/${RANDOM_HTTP_PORT}'"/$RANDOM_HTTP_PORT/" conf/application.conf; echo random port is $RANDOM_HTTP_PORT
RUN /play/play autotest || true


FROM debian:bullseye-slim as tomcat
# Copy Java
COPY --from=base /java /java
ENV JAVA_HOME=/java
ENV PATH $JAVA_HOME/bin:$PATH


# Mimic Tomcat image (copy-paste from https://github.com/docker-library/tomcat)
ENV CATALINA_HOME /tomcat
ENV PATH $CATALINA_HOME/bin:$PATH
RUN mkdir -p "$CATALINA_HOME"
WORKDIR $CATALINA_HOME

# let "Tomcat Native" live somewhere isolated
ENV TOMCAT_NATIVE_LIBDIR $CATALINA_HOME/native-jni-lib
ENV LD_LIBRARY_PATH ${LD_LIBRARY_PATH:+$LD_LIBRARY_PATH:}$TOMCAT_NATIVE_LIBDIR

COPY --from=base $CATALINA_HOME $CATALINA_HOME
RUN set -eux; \
	apt-get update; \
	xargs -rt apt-get install -y --no-install-recommends < "$TOMCAT_NATIVE_LIBDIR/.dependencies.txt"; \
	rm -rf /var/lib/apt/lists/*

# verify Tomcat Native is working properly
RUN set -eux; \
	nativeLines="$(catalina.sh configtest 2>&1)"; \
	nativeLines="$(echo "$nativeLines" | grep 'Apache Tomcat Native')"; \
	nativeLines="$(echo "$nativeLines" | sort -u)"; \
	if ! echo "$nativeLines" | grep -E 'INFO: Loaded( APR based)? Apache Tomcat Native library' >&2; then \
		echo >&2 "$nativeLines"; \
		exit 1; \
	fi

EXPOSE 8080
CMD ["catalina.sh", "run"]

RUN test -s HealthCheck.class
HEALTHCHECK --start-period=3s CMD [ "java", "-cp", ".", "HealthCheck", "8080", "||", "exit", "1" ]


FROM tomcat
# Default values for config environment variables
ENV OPENSEEDBOX_JDBC_URL=jdbc:postgresql://openseedboxdb/openseedbox
ENV OPENSEEDBOX_JDBC_DRIVER=org.postgresql.Driver
ENV OPENSEEDBOX_JDBC_USER=openseedbox
ENV OPENSEEDBOX_JDBC_PASS=openseedbox
ENV OPENSEEDBOX_CONTEXT_PATH=
ENV OPENSEEDBOX_SESSION_COOKIE_NAME=NOT_GENERATED
ENV APPLICATION_SECRET=NOT_GENERATED
ENV GOOGLE_CLIENTID=

# Copy WAR file from builder container to the final image
COPY --from=builder /src/war $CATALINA_HOME/webapps/ROOT
