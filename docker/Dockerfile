FROM java:6-jre

ENTRYPOINT /run.sh

# Default values for config environment variables
ENV OPENSEEDBOX_DATABASE_NAME=openseedbox
ENV HTTP_PORT=9000
ENV MYSQL_HOST=mysql
ENV MYSQL_PORT=3306
ENV MYSQL_USER=openseedbox
ENV MYSQL_PASS=openseedbox

EXPOSE ${HTTP_PORT}

# Install play
RUN wget -q -O play.zip "https://downloads.typesafe.com/play/1.2.7.2/play-1.2.7.2.zip" \
	&& unzip -q play.zip \
	&& mv /play-1.2.7.2 /play
RUN apt-get -qq update && apt-get install -qq -y git python && apt-get -y clean

# Check out openseedbox and resolve dependencies
RUN mkdir /src && cd /src \
	&& git clone -q https://github.com/erindru/openseedbox-common.git \
	&& git clone -q https://github.com/erindru/openseedbox-server \
	&& git clone -q https://github.com/erindru/openseedbox
RUN cd /src/openseedbox && /play/play deps \
	&& cd /src/openseedbox-server && /play/play deps

VOLUME /src/openseedbox/conf

COPY application.conf /src/openseedbox/conf/application.conf

COPY run.sh /run.sh
