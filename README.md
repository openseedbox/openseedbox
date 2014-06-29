OpenSeedbox - Open Source Seedbox UI - http://www.openseedbox.com

Comprised of the following projects:

* Common Code (openseedbox-common) : https://github.com/erindru/openseedbox-common
* Frontend (openseedbox) : https://github.com/erindru/openseedbox
* Backend (openseedbox-server) : https://github.com/erindru/openseedbox-server

Overview
--------
OpenSeedbox is a web-based UI for bittorrent. It is designed to be multi-user and make use of existing bittorrent clients to do the actual downloading. Currently only transmission-daemon is supported as a backend but more can be implemented via the ITorrentBackend interface.

**OpenSeedbox is still very experimental and really only intended for developers at the moment!**

Features
--------
OpenSeedbox supports to varying degrees:

* Multiple users in a single unified environment
* The concept of "Plans" which limit what a user can do (how much space they can use, how many torrents they can have active)
* The concept of "Nodes" which when registered, get torrent add/remove/status requests farmed out to them
* An JSON-based API for making third-party clients (eg mobile apps)
* The ability to give users a dedicated node (recommended at the moment since the node choosing algorithm has not been implemented yet)
* The ability to group torrents in the UI
* The ability to download torrents as a zip on-the-fly *with resume support* (if NGINX is compiled with the mod_zip extension)

Dependencies
------------
OpenSeedbox only runs in a Linux environment at this time due to linux-specific commands being executed within it

* openjdk 1.6 (1.7 causes issues and 1.5 is too old)
* playframework 1.2.5
* transmission-daemon 2.51 or greater (backend only)
* nginx compiled with mod_zip and headers_more module (if you want zip support), otherwise Apache will work too (youll need X-Sendfile support if you use Apache)

Architecture
------------
OpenSeedbox is comprised of two parts - the Frontend and the Backend. The Frontend stores its data in a MySQL database and maintains the system state. The Backend has no database and is only concerned with doing what the Frontend tells it. It is possible to run the frontend and the backend on the same server but I dont recommend it.

**The Frontend**

This is a Play! framework application (and thus is written in Java). It is designed to be proxied to via a proper webserver (such as Apache or NGINX). The Frontend is what the user interacts with and is what allows users to log in and download torrents. It also allows an Admin to check the status of the nodes, set up plans, put users on plans and check that the background jobs are running properly.

**The Backend**

This is also a Play! framework application and is responsible for delegating requests to a torrent client running on the same host (at the moment this is transmission-daemon) and also reporting the system status of the host. It is designed to be "dumb", ie no state is stored on the backend, which torrents belong to which users is handled by the frontend. The Backend is run on as many nodes as you want the system to have.

Installation
------------
This is the hard part and really needs to be refined. Note: it is recommended to use HTTPS everywhere to stop ISP's prying on yours/your users traffic, and also specifying an encrypted location for *openseedbox.base.path* on the backend to stop server providers scanning the hard drive and finding files that they can use as an excuse to terminate your server.

** Installing the Frontend **

1. Install openjdk1.6, [Play 1.2.5](http://downloads.typesafe.com/releases/play-1.2.5.zip) and mysql-server
	
	Openjdk1.6

	`sudo apt-get install openjdk-6-jdk`

	Play
	
	1.1 Download Play framework to Downloads folder.
	
	`cd ~/Downloads`
	
	`wget http://download.playframework.org/releases/play-1.2.5.zip`
	
	1.2 Unzip the archive and move the contents to `/usr/local` folder.
	
	`unzip play-1.2.5.zip`
	
	`sudo mv play-1.2.5 /usr/local/`
	
	1.3 Configure access to framework via link
	
	`sudo ln -s /usr/local/play-1.2.5/ /usr/local/play`
	
	1.4 Create links in `/usr/local/bin` so that play command will be available in terminal
	
	`sudo ln -s /usr/local/play/play /usr/local/bin/play`
	
	
	MySql server
	
	`sudo apt-get install mysql-server`
	
2. Verify Play! is working by running `play` and checking the version number
3. Checkout the source for *openseedbox-common* and *openseedbox* to a common location (I use */src*):
	
	`cd /src`
	
	`git clone https://github.com/erindru/openseedbox.git`
	
	`git clone https://github.com/erindru/openseedbox-common.git`	
4. Create a mysql database, I call mine `openseedbox`
5. Rename application.conf.default to application.conf, eg:

	`mv /src/openseedbox/application.conf.default /src/openseedbox/application.conf`
6. Edit the newly created application.conf with your database settings (look at the db.* lines)

	See the configuration reference below for more information on the openseedbox-specific options
7. Run `play deps` to fetch the dependencies, eg:

	`cd /src/openseedbox-common && play deps`
	
	`cd /src/openseedbox && play deps`
8. Start the app in production mode (youll need to wait a bit till everything compiled)

	`cd /src/openseedbox && play start --%prod`
	
9. Try going to http://localhost:9000 in your browser

10. Set up NGINX or Apache as a reverse proxy (sample config for nginx in `conf/openseedbox.nginx.conf`)

Once you login successfully, you will need to manually edit the User table in the database and set is_admin=1 for your user or you wont be able to add any nodes. Note: You need to install the backend on the nodes *before* you add them in the web UI.

**Installing the Backend**

1. Install openjdk1.6 and [Play 1.2.5](http://downloads.typesafe.com/releases/play-1.2.5.zip)
2. Verify Play! is working by running `play` and checking the version number
3. Checkout the source for *openseedbox-common* and *openseedbox-server* to a common location (I use */src*):
	
	`cd /src`
	
	`git clone https://github.com/erindru/openseedbox-server.git`
	
	`git clone https://github.com/erindru/openseedbox-common.git`	
4. Rename application.conf.default to application.conf, eg:

	`mv /src/openseedbox-server/application.conf.default /src/openseedbox-server/application.conf`
	
5. Edit the newly created application.conf with your node-specific backend settings (see the Configuration reference below). Take special note of the `backend.base.api_key` you set.

6. Run `play deps` to fetch the dependencies, eg:

	`cd /src/openseedbox-common && play deps`
		
	`cd /src/openseedbox-server && play deps`	
7. Start the app in production mode (youll need to wait a bit till everything compiled)

	`cd /src/openseedbox-server && play start --%prod`
	
9. Try going to http://localhost:9001 in your browser

10. Set up NGINX or Apache as a reverse proxy (sample config for nginx in `conf/openseedbox-server.nginx.conf`). I really recommend NGINX here as it is absolutely required to use the ZIP file functionality.

**Compiling NGINX**

If you're going to use a custom version of nginx (recommended if you want ZIP to work), I do the following (in */src*)

1. Get the [NGINX source](http://nginx.org/download/nginx-1.2.6.tar.gz), [mod_zip source](https://github.com/evanmiller/mod_zip) and [headers_more](https://github.com/agentzh/headers-more-nginx-module) source and put them in */src*
2. Use the following command to configure nginx:

	`./configure --with-http_ssl_module --add-module=/src/mod_zip/ --prefix=/etc/nginx --conf-path=/etc/nginx/nginx.conf --error-log-path=/var/log/nginx/error.log --pid-path=/var/run/nginx.pid --http-log-path=/var/log/nginx/access.log --lock-path=/var/lock/nginx.lock --sbin-path=/usr/sbin/nginx --add-module=/src/headers-more-nginx-module`

3. Finish the install

	`make && make install`
	
Configuration Reference
-----------------------

**Frontend**

NGINX sample configuration is in `openseedbox/conf/openseedbox.nginx.conf`

Configuration is in `openseedbox/conf/application.conf`

* `openseedbox.node.access` - 'http' or 'https'. If you're using SSL on your backend nodes, use https, otherwise use http.
* `openseedbox.errors.mailto` - an email address. Errors will get sent here.
* `openseedbox.errors.mailfrom` - the From address in the error emails
* `openseedbox.zip` - 'true' or 'false'. If 'false', no zip options are shown in the UI
* `openseedbox.zip.path` - the internal NGINX path for multi-torrent zips. Is '/rdr' in the default nginx config.
* `openseedbox.zip.manifestonly` - 'true' or 'false'. Only set to 'true' for debugging multi-torrent zips.
* `openseedbox.assets.prefix` - Used as the prefix to all asset URLs. This is so you can upload all the assets (the /public directory) to something like Amazon S3 and have them served from there.

**Backend**

NGINX sample configuration is in `openseedbox-server/conf/openseedbox-server.nginx.conf`

Configuration is in `openseedbox-server/conf/application.conf`

* `backend.base.api_key` - The API key used by the Frontend to communicate with the backend. Whatever you put 		here, you will need to enter when setting up the node in the Frontend.
* `backend.base.path` - Essentially, the torrent download location. I tend to use '/media/openseedbox` since I have an encrypted partition mounted there
* `backend.base.path.encrypted` - 'true' or 'false'. Set to 'true' if your backend.base.path is an encrypted location. If its 'true', the backend will throw errors if the encrypted partition is not mounted to prevent unencrypted data being written.
* `backend.base.device` - The device that shows up in 'df -h' that your torrents are being downloaded to. This is used to determine free/used space on the node in the frontend Admin interface
* `backend.class` - Should be 'com.openseedbox.backend.transmission.TransmissionBackend' unless you have implemented your own backend.
* `backend.download.scheme` - 'http' or 'https'. If you're using SSL, set to 'https', otherwise 'http'.
* `backend.download.xsendfile` - 'true' or 'false'. Only turn it off if you want Play! to serve files directly, which is a Bad Idea.
* `backend.download.xsendfile.path` - The internal path that files that are being X-Sendfile'd are located at
* `backend.download.xsendfile.header` - 'X-Accel-Redirect' or 'X-Sendfile' if you're using NGINX or Apache as your webservers respectively.
* `backend.download.ngxzip` - 'true' or 'false'. Only works if NGINX is your webserver, enables/disables zip downloading
* `backend.download.ngxzip.path` - The internal path to files, used in the ZIP manifest (tyically the same as backend.download.xsendfile.path)
* `backend.download.ngxzip.manifestonly` - 'true' or 'false', only set to true for debugging purposes
* `backend.blocklist.url` - Set to the URL of an IP blocklist for bad peers, eg 'http://list.iblocklist.com/?list=bt_level1&fileformat=p2p&archiveformat=gz'. This URL is loaded into the torrent backend if blocklists are supported.
* `openseedbox.assets.prefix` - Same as in the Frontend config
