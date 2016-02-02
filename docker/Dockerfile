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
	&& mv /play-1.2.7.2 /play
RUN apt-get -qq update \
	&& apt-get install -qq -y git python supervisor build-essential openssl libpcre3-dev libssl-dev \
	&& apt-get -y clean

WORKDIR /src

# Check out code we rely on
RUN git clone -q https://github.com/erindru/openseedbox-common.git \
	&& git clone --depth=1 -q https://github.com/erindru/openseedbox-server \
	&& git clone --depth=1 -q https://github.com/erindru/openseedbox \
	&& git clone --depth=1 -q https://github.com/evanmiller/mod_zip \
	&& git clone --depth=1 -q https://github.com/agentzh/headers-more-nginx-module

# Install openseedbox dependencies
RUN cd openseedbox-common && /play/play deps
RUN cd openseedbox && /play/play deps
RUN cd openseedbox-server && /play/play deps

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

#specify persistent volumes
#VOLUME /src/openseedbox/conf

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
