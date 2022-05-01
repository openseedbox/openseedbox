FROM balenalib/#{ARCH}-debian:buster

ENTRYPOINT /usr/bin/supervisord

# Default values for config environment variables
ENV OPENSEEDBOX_JDBC_URL=jdbc:postgresql://openseedboxdb/openseedbox
ENV OPENSEEDBOX_JDBC_DRIVER=org.postgresql.Driver
ENV OPENSEEDBOX_JDBC_USER=openseedbox
ENV OPENSEEDBOX_JDBC_PASS=openseedbox

EXPOSE 443

ENV JAVA_HOME=/java

#{BALENA_CROSSBUILD_BEGIN}

# See https://github.com/resin-io-library/base-images/issues/273
# "Errors installing OpenJDK due to unexistent man pages directory"
#RUN mkdir /usr/share/man/man1

# Install runtime packages
RUN apt-get -qq update \
	&& apt-get -qq install -y --no-install-recommends \
		curl wget unzip git \
		python supervisor \
		zlibc zlib1g \
	&& apt-get -y clean \
	&& rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*

# Install Temurin JDK from Adoptium
RUN apt-get -qq update && apt-get -qq install -y gnupg \
	&& wget -qO - https://packages.adoptium.net/artifactory/api/gpg/key/public | apt-key add - \
	&& echo "deb https://packages.adoptium.net/artifactory/deb $(awk -F= '/^VERSION_CODENAME/{print$2}' /etc/os-release) main" | tee /etc/apt/sources.list.d/adoptium.list \
	&& apt-get -qq update \
	&& apt-get -qq install -y temurin-11-jdk \
	&& jlink --add-modules ALL-MODULE-PATH --output /java/ --strip-debug --no-man-pages --compress=2 \
	&& apt-get -qq purge -y gnupg temurin-11-jdk \
	&& rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*

# Install play
ENV PLAY_VERSION=1.3.4
RUN wget -q "https://downloads.typesafe.com/play/${PLAY_VERSION}/play-${PLAY_VERSION}.zip" \
	&& unzip -q play-${PLAY_VERSION}.zip \
	&& mv /play-${PLAY_VERSION} /play \
	&& rm play-${PLAY_VERSION}.zip

# Install siena module to play
RUN /play/play install siena-2.0.7 || echo "Downloading directly ... " \
	&& curl -S -s -L -o siena-2.0.7.zip "https://www.playframework.com/modules/siena-2.0.7.zip" \
	&& for zipfile in *.zip; do module="${zipfile%.zip}"; unzip -d /play/modules/"$module" "$zipfile"; rm "$zipfile"; done;

WORKDIR /src

# Check out code we rely on and install play! dependencies
RUN git clone -q https://github.com/openseedbox/openseedbox-common \
	&& git clone --depth=1 -q https://github.com/openseedbox/openseedbox \
	&& /play/play deps openseedbox-common --sync \
	&& /play/play deps openseedbox --sync

# Download and compile nginx
RUN apt-get -qq update \
	&& apt-get -qq install -y \
		#{BUILD_DEPS} \
	&& apt-get -y clean \
	&& rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/* \
	&& git clone --depth=1 -q https://github.com/evanmiller/mod_zip \
	&& git clone --depth=1 -q https://github.com/agentzh/headers-more-nginx-module \
	&& wget -q -O nginx.tar.gz http://nginx.org/download/nginx-1.14.2.tar.gz \
	&& tar -xf nginx.tar.gz \
	&& cd nginx* \
	&& ./configure --with-http_ssl_module --add-module=/src/mod_zip/ \
		--prefix=/etc/nginx --conf-path=/etc/nginx/nginx.conf \
		--error-log-path=/var/log/nginx/error.log --pid-path=/var/run/nginx.pid \
		--http-log-path=/var/log/nginx/access.log --lock-path=/var/lock/nginx.lock \
		--sbin-path=/usr/sbin/nginx --add-module=/src/headers-more-nginx-module \
	&& make \
	&& make -s install \
	&& cd .. \
	&& rm -fr nginx* mod_zip headers-more-nginx-module \
	&& apt-get -qq purge -y \
		#{BUILD_DEPS} \
	&& apt-get -y autoremove \
	&& apt-get -y clean \
	&& rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*

#create SSL keys
RUN openssl req -new -newkey rsa:4096 -days 365 -nodes -x509 -subj "/C=US/ST=None/L=None/O=None/CN=openseedbox" \
	-keyout /src/openseedbox/conf/host.key \
	-out /src/openseedbox/conf/host.cert

#{BALENA_CROSSBUILD_END}

#copy config files
COPY application.conf /src/openseedbox/conf/application.conf
COPY nginx.conf /etc/nginx/nginx.conf
COPY supervisord.conf /etc/supervisor/conf.d/supervisord.conf

#copy run script
COPY run.sh /run.sh

WORKDIR /src/openseedbox
