#!/bin/bash

function install_package() {
	echo "Checking if $1 is installed"
	installed=`apt-cache policy $1 | grep "(none)"`
	if [ "$installed" != "" ]; then
		echo "$1 is not installed; installing."
		apt-get install $1 -qq --assume-yes
	else
		echo "$1 is already installed; moving on."
	fi
}

#Parameters
openseedbox_username=$SUDO_USER
base_dir="${config.baseFolder}"
nginx_base="/etc/nginx/"
nginx_site_config="$nginx_base/sites-available/openseedbox"
nginx_enabled_site_config="$nginx_base/sites-enabled/openseedbox"
required_packages="nginx git transmission-daemon"

#NGINX config
nginx_config="
server {
	listen 443;
	
	server_name ${config.ipAddress};

	ssl on;
	ssl_certificate /home/$openseedbox_username/host.cert;
	ssl_certificate_key /home/$openseedbox_username/host.key;

	root ${config.baseFolder};
	index index.html;

	location /protected/ {
		internal;
		alias /;
	}
}"


################ SCRIPT START ###################################

#Check for root; script should only be run as root
root=`whoami`
if [ "$root" != "root" ]; then
	echo "Script needs to be run as root, not $root. Exiting."
	exit
fi

if [ $SUDO_USER == "" ]; then
	echo "Script needs to be run using sudo -i (to set SUDO_USER). Exiting."
fi

echo "Openseedbox user will be $SUDO_USER"

#update apt package archive
echo "Updating apt package list (quietly, please wait...)"
apt-get update -qq

#install required packages
echo -e "-- INSTALLING REQUIRED PACKAGES --"
for package in $required_packages; do
	install_package $package
done
echo -e "-- FINISHED INSTALLING REQUIRED PACKAGES--\n"

#create openseedbox folders, set permissions and checkout git repo
echo -e "-- SETTING UP OPENSEEDBOX SERVER FILES --"
echo "Creating directory $base_dir..."
mkdir -p $base_dir
echo "Setting permissions on $base_dir..."
chown -R "$openseedbox_username" "$base_dir"
echo -e " -- FINISHED SETTING UP OPENSEEDBOX SERVER FILES --\n"

#set up apache to enable xsendfile mod and also setup default website. then restart apache
echo -e "-- SETTING UP NGINX --"

echo "Installing server keys..."
cp ~/host.cert "$base_dir/host.cert"
cp ~/host.key "$base_dir/host.key"

echo "Writing out site config..."
echo "$nginx_config" > $nginx_site_config

echo "Enabling site..."
ln -fs $nginx_site_config $nginx_enabled_site_config

echo "Restarting apache..."
service nginx restart

echo -e "-- FINISHED SETTING UP NGINX --\n"
echo "All done."