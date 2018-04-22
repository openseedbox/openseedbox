FROM #{FROM}

ENTRYPOINT /usr/bin/supervisord

# Default values for config environment variables
ENV OPENSEEDBOX_JDBC_URL=jdbc:postgresql://openseedboxdb/openseedbox
ENV OPENSEEDBOX_JDBC_DRIVER=org.postgresql.Driver
ENV OPENSEEDBOX_JDBC_USER=openseedbox
ENV OPENSEEDBOX_JDBC_PASS=openseedbox

EXPOSE 443

# See https://github.com/resin-io-library/base-images/issues/273
# "Errors installing OpenJDK due to unexistent man pages directory"
RUN for i in 1 2 3 4 5 6 7 8; do mkdir -p /usr/share/man/man$i; done;

#{CUSTOM_QEMU}

# Install runtime packages
RUN apt-get -qq update \
	&& apt-get -qq install -y \
		curl wget unzip git openjdk-7-jre-headless \
		python supervisor \
	&& apt-get -y clean \
	&& rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*

# Install play
RUN wget -q -O play.zip "https://downloads.typesafe.com/play/1.3.4/play-1.3.4.zip" \
	&& unzip -q play.zip \
	&& mv /play-1.3.4 /play \
	&& rm play.zip

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
	&& wget -q -O nginx.tar.gz http://nginx.org/download/nginx-1.14.0.tar.gz \
	&& tar -xf nginx.tar.gz \
	&& cd nginx* \
	&& ./configure --with-http_ssl_module --add-module=/src/mod_zip/ \
		--prefix=/etc/nginx --conf-path=/etc/nginx/nginx.conf \
		--error-log-path=/var/log/nginx/error.log --pid-path=/var/run/nginx.pid \
		--http-log-path=/var/log/nginx/access.log --lock-path=/var/lock/nginx.lock \
		--sbin-path=/usr/sbin/nginx --add-module=/src/headers-more-nginx-module \
	&& make -s \
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

#copy config files
COPY application.conf /src/openseedbox/conf/application.conf
COPY nginx.conf /etc/nginx/nginx.conf
COPY supervisord.conf /etc/supervisor/conf.d/supervisord.conf

#copy run script
COPY run.sh /run.sh

WORKDIR /src/openseedbox
