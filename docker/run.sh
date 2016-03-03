#!/bin/bash
cd /src/openseedbox

APPLICATION_SECRET=`grep "application.secret=NOT_GENERATED" conf/application.conf`

if [ "$APPLICATION_SECRET"  != "" ]; then
	echo "Play secret has not been generated; generating"
	/play/play secret
fi

if [ "$GOOGLE_CLIENTID" == "" ]; then
	echo "You need to specify your Google ClientID in the GOOGLE_CLIENTID environment variable or you wont be able to log in"
	exit 1
fi

echo "Starting play"
exec /play/play run
