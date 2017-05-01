FROM java:6-jre

ENTRYPOINT /usr/bin/supervisord

# Default values for config environment variables
ENV OPENSEEDBOX_DATABASE_NAME=openseedbox
ENV MYSQL_HOST=mysql
ENV MYSQL_PORT=3306
ENV MYSQL_USER=openseedbox
ENV MYSQL_PASS=openseedbox

EXPOSE 443

# Install play
RUN wget -q -O play.zip "https://downloads.typesafe.com/play/1.2.7.2/play-1.2.7.2.zip" \
	&& unzip -q play.zip \
	&& mv /play-1.2.7.2 /play \
	&& rm play.zip

# Install other needed packages
RUN apt-get -qq update \
	&& apt-get install -qq -y git procps python supervisor build-essential openssl libpcre3-dev libssl-dev \
	&& apt-get -y clean \
	&& rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*

# Install siena module to play
RUN /play/play install siena-2.0.7 || echo "Downloading directly ... " \
	&& curl -S -s -L -o siena-2.0.7.zip "https://www.playframework.com/modules/siena-2.0.7.zip" \
	&& for zipfile in *.zip; do module="${zipfile%.zip}"; unzip -d /play/modules/"$module" "$zipfile"; rm "$zipfile"; done;

WORKDIR /src

# Check out code we rely on and install play! dependencies
RUN git clone -q https://github.com/openseedbox/openseedbox-common \
	&& git clone --depth=1 -q https://github.com/openseedbox/openseedbox \
	&& git clone --depth=1 -q https://github.com/evanmiller/mod_zip \
	&& git clone --depth=1 -q https://github.com/agentzh/headers-more-nginx-module \
	&& /play/play deps openseedbox-common --sync \
	&& /play/play deps openseedbox --sync

# Download and compile nginx
RUN wget -q -O nginx.tar.gz http://nginx.org/download/nginx-1.9.9.tar.gz \
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
	&& rm -fr nginx* mod_zip headers-more-nginx-module

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
