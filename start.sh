#!/bin/bash
if [ "$1" == "" ]; then
	echo "No instance name specified, exiting."
	exit
fi
instance_name=$1
echo "Getting latest code"
git pull
echo "Stopping play server"
play stop
echo "Removing server.pid"
rm server.pid
echo "Cleaning up the crap"
play clean
echo "Getting latest dependencies"
play deps
echo "Starting play server ($instance_name)"
play start --%$instance_name

