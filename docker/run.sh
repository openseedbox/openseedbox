#!/bin/bash
cd /src/openseedbox

if [ `grep "application.secret=NOT_GENERATED" conf/application.conf` != "" ]; then
	echo "Play secret has not been generated; generating"
	/play/play secret
fi

if [ "$GOOGLE_CLIENTID" == "" ]; then
	echo "You need to specify your Google ClientID in the GOOGLE_CLIENTID environment variable or you wont be able to log in"
	exit 1
fi

#add any nodes defined in the OPENSEEDBOX_NODES environment variable.
#format is: "node1:port node2:port". Typically node1 and node2 would be the name of linked Docker containers.
if [ "$OPENSEEDBOX_NODES" != "" ]; then
	for NODE in $OPENSEEDBOX_NODES; do
		HOST=`echo $NODE | cut -d ":" -f 1`
		PORT=`echo $NODE | cut -d ":" -f 2`
		API_KEY="$HOST"

		MYSQL_CMD="mysql -u'$MYSQL_USER' -p'$MYSQL_PASS'"

		echo "Adding node $NODE"
		$NODE_EXISTS = `$MYSQL_CMD -e "SELECT * FROM node WHERE ip_address='$HOST'" $OPENSEEDBOX_DATABASE_NAME`
		if [ $NODE_EXISTS == "" ]; then
			$MYSQL_CMD -e "INSERT INTO node (name,ip_address,scheme,api_key,active) VALUES ('$HOST','$HOST:$PORT','http','$HOST',1)" $OPENSEEDBOX_DATABASE_NAME
		else
			echo "Node $NODE already added; not adding again"
		fi
	done
fi

echo "Starting play"
exec /play/play run
