#!/bin/bash

function install_package() {
	echo "Checking if $1 is installed"
	installed=`apt-cache policy $1 | grep "(none)"`
	if [ "$installed" != "" ]; then
		echo "$1 is not installed; installing."
		apt-get install $1 --quiet --assume-yes
	else
		echo "$1 is already installed; moving on."
	fi
}

#Parameters
openseedbox_username="openseedbox"
base_dir="/openseedbox"
http_root="$base_dir/http"
apache2_base="/etc/apache2";
apache2_site_config="$apache2_base/sites-available/openseedbox"
required_packages="apache2 libapache2-mod-php5 libapache2-mod-xsendfile git transmission-daemon"

#Apache config
apache_config="
<VirtualHost *:443>
	ServerAdmin support@openseedbox.com
	
	XSendFile on
	XSendFilePath /openseedbox
	
	DocumentRoot $http_root
	
	SSLEngine on
	SSLCertificateFile /openseedbox/host.cert
	SSLCertificateKeyFile /openseedbox/host.key
	
	Alias /openseedbox-server "$http_root"
	<Directory $http_root>
		SSLRequireSSL
		Options Indexes FollowSymLinks MultiViews
		AllowOverride All
		Order allow,deny
		allow from all
	</Directory>
</VirtualHost>"


################ SCRIPT START ###################################

#Check for root; script should only be run as root
root=`whoami`
if [ "$root" != "root" ]; then
	echo "Script needs to be run as root, not $root. Exiting."
	exit
fi

#update apt package archive
echo "Updating apt package list (quietly, please wait...)"
apt-get update -qq

#install required packages
echo -e "-- INSTALLING REQUIRED PACKAGES --"
for package in $required_packages; do
	install_package $package
done
echo -e "-- FINISHED INSTALLING REQUIRED PACKAGES--\n"

#set up openseedbox user
echo -e "\n-- SETTING UP OPENSEEDBOX USER --"
echo "Creating user $openseedbox_username..."
exists=`cat /etc/passwd | grep $openseedbox_username`
if [ "$exists" == "" ]; then
	useradd $openseedbox_username
	echo "Setting user password..."
	passwd $openseedbox_username	
else
	echo "User already exists; skipping."
fi

echo "Creating group $openseedbox_username..."
exists=`cat /etc/group | grep $openseedbox_username`
if [ "$exists" == "" ]; then
	groupadd $openseedbox_username
else
	echo "Group already exists; skipping."
fi

echo "Adding $openseedbox_username user to $openseedbox_username and www-data groups..."
usermod -a -G "openseedbox,www-data" openseedbox

echo -e "-- FINISHED SETTING UP OPENSEEDBOX USER --\n"

#create openseedbox folders, set permissions and checkout git repo
echo -e "-- SETTING UP OPENSEEDBOX SERVER FILES --"
echo "Creating directory $http_root..."
mkdir -p $http_root
echo "Setting permissions on $base_dir..."
chown -R "$openseedbox_username" "$base_dir"
if [ ! -d "$http_root/.git" ]; then
	echo "Checking out server code into $http_root..."
	git clone https://unsignedint@bitbucket.org/unsignedint/openseedbox-server.git $http_root
else
	echo "Updating server code in $http_root..."
	cd $http_root
	git pull
fi
echo -e " -- FINISHED SETTING UP OPENSEEDBOX SERVER FILES --\n"

#set up apache to enable xsendfile mod and also setup default website. then restart apache
echo -e "-- SETTING UP APACHE2 --"
echo "Checking xsendfile is enabled..."
if [ ! -f "$apache2_base/mods-enabled/xsendfile.load" ]; then
	echo "...it isnt, enabling."
	a2enmod xsendfile
fi

echo "Checking ssl is enabled..."
if [ ! -f "$apache2_base/mods-enabled/ssl.load" ]; then
	echo "...it isnt, enabling."
	a2enmod ssl
fi

echo "Retrieving server keys..."
wget -N http://cdn.openseedbox.com/other/host.cert -O "$base_dir/host.cert"
wget -N http://cdn.openseedbox.com/other/host.key -O "$base_dir/host.key"

echo "Writing out site config..."
echo "$apache_config" > $apache2_site_config

echo "Disabling Port 80"
sed -i "s/^NameVirtualHost/#NameVirtualHost/g" "$apache2_base/ports.conf"
sed -i "s/^Listen 80/#Listen 80/g" "$apache2_base/ports.conf"

echo "Enabling site..."
a2dissite 000-default #this site interferes if its not disabled
a2ensite openseedbox

echo "Changing apache user to $openseedbox_username..."
sed -i "s/www-data/$openseedbox_username/g" /etc/apache2/envvars

echo "Restarting apache..."
service apache2 restart

echo -e "-- FINISHED SETTING UP APACHE2 --\n"
echo "All done."

