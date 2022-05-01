#!/bin/bash

APPLICATION_SECRET=`grep "application.secret=NOT_GENERATED" conf/application.conf`
GOOGLE_CLIENTID_NEEDED=`grep "google.clientid=" conf/application.conf`
SESSION_COOKIE_NAME=`grep "application.session.cookie=NOT_GENERATED" conf/application.conf`

if [ "$APPLICATION_SECRET"  != "" ]; then
	echo "Play secret has not been generated; generating"
	/play/play secret
fi

if [ "$GOOGLE_CLIENTID" == "" -a -n "$GOOGLE_CLIENTID_NEEDED" ]; then
	echo "You need to specify your Google ClientID in the GOOGLE_CLIENTID environment variable or you wont be able to log in"
	exit 1
fi

if [ "$SESSION_COOKIE_NAME"  != "" ]; then
	echo -n "Play session cookie name has not been generated; generating ... "
	application_name=`grep "application.name=" conf/application.conf | cut -d= -f2-`
	new_cookie_name=`basename \`mktemp -u PLAY_${application_name}_XXXXXXXXXXXXXXXX\` | tr [a-z] [A-Z]`
	sed -i -e s~$SESSION_COOKIE_NAME~application.session.cookie=$new_cookie_name~g conf/application.conf
	echo "$new_cookie_name"
fi

echo "Starting play in $PWD"

DOCKER_CGROUP=`basename $(head -n1 /proc/self/cgroup)`
VOLUME_CGROUP_FILES=`ls /cgroup 2>/dev/null`
SYSFS_CGROUP_FILES=`ls /sys/fs/cgroup 2>/dev/null`
CGROUP_MOUNT="'-v /sys/fs/cgroup/:/cgroup:ro'"
if [ -z "$SYSFS_CGROUP_FILES" ]; then
	if [ -n "$VOLUME_CGROUP_FILES" ]; then
		for cgroup_line in `find /cgroup -wholename *$DOCKER_CGROUP -type d` ;
		do
			cgroup_type=`echo $cgroup_line|cut -d/ -f3`
			echo Setting cgroup limit: $cgroup_type
			ln -s $cgroup_line /sys/fs/cgroup/$cgroup_type
		done;
	else
		echo "WARNING: Could not enforce automatically cgroup memory limit to container"
		echo "WARNING: Try running the container with $CGROUP_MOUNT option"
	fi;
fi;

exec /play/play run -XshowSettings:vm
