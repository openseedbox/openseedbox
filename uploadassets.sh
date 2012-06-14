#!/bin/bash

#parameters
bucket_name="cdn.openseedbox.com"

echo -n "Checking s3cmd is installed..." 
have_s3cmd=`which s3cmd`
if [ "$have_s3cmd" == "" ]; then
	echo -e "\nNo s3cmd detected, installing."
	sudo apt-get install s3cmd
else
	echo " it is."
fi

echo -n "Checking s3cmd is configured..."
if [ ! -f "$HOME/.s3cfg" ]; then
	s3cmd --configure
else 
	echo -n -e " it is.\n"
fi

echo "Syncing /public with $bucket_name/public"
s3cmd sync --acl-public "public/" "s3://$bucket_name/public/"

echo "All done."
