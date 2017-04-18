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
Installation consists of:

1. Obtaining a Google ClientID so you can use google logins (the only type of login)
1. Setting up the frontend
1. Setting up 1 or more backends
1. Telling the frontend about the backends
1. Configuring some plans and users

It is recommended to mount an encrypted volume to `/media/openseedbox` on each backend node to stop server providers scanning the hard drive and finding files that they can use as an excuse to terminate your server. However it is not required in order for Openseedbox to function.

**Setting up an application in the Google Developers Console**

Openseedbox uses google logins. In order for this to work, you need to create a project in the Google Developers Console and add the URL to your app as an allowed origin.

1. Go to the [Google Developers Console](https://console.developers.google.com/project) and create a new project for your Openseedbox instance. Go to the project.
2. Go to "Dashboard" => "Enable and manage APIs", and enter "Google+" in the search field. Click the "Google+ API" result and click the "Enable API" button.
3. On the left, click "Credentials" => "New credentials" => "OAuth Client ID". Choose "Web Application" and in "Authorized Javascript Origins" add the domain for your install of Openseedbox (eg, "https://localhost/" or "https://my.public.openseedbox.domain/")
4. Click "Create" and make a note of the "Client ID" value for later use.

**Docker**

The only supported installation method of Openseedbox is to use the Docker images. This guide assumes you have a working Docker daemon. If you do not, please see [here](https://docs.docker.com/engine/installation/) in order to get it installed.

*Note:* Docker does not work on OpenVZ VPS's, so if you have a VPS on which you want to run Openseedbox, you need a KVM VPS.

**MySQL**

1. OpenSeedbox needs a MySQL server available. If you have one running somewhere then you can set the OPENSEEDBOX_DATABASE_NAME, MYSQL_HOST, MYSQL_PORT, MYSQL_USERNAME and MYSQL_PASSWORD environment variables to tell Openseedbox how to connect to it. If you dont have one running somewhere, then you can easily start one:

	`docker run --name openseedbox-mysql -e MYSQL_ROOT_PASSWORD=password -e MYSQL_USER=openseedbox -e MYSQL_PASSWORD=openseedbox -e MYSQL_DATABASE=openseedbox -d mysql`

*Note* if you change the username/password from the defaults, then you'll need to specify them as environment variables below.

**PostgreSQL**
1. OpenSeedbox also supports PostgreSQL as database server. Here is a `docker` command to start one:

	`docker run --name openseedbox-postgres -e POSTGRES_USER=openseedbox -e POSTGRES_PASSWORD=openseedbox -e POSTGRES_DB=openseedbox -d postgres`

*Note:* You should configure the Frontend manually to connect to the PostgreSQL database!

**Installing the Frontend**

1. The following command will start an openseedbox container and map it to port 443 on your host:

	`docker run --name openseedbox --link openseedbox-mysql:mysql -p 443:443 -e GOOGLE_CLIENTID=<the google clientid you got above> -d openseedbox/client`

	If you arent using the 'quick n dirty' MySQL server above, then you'll need to run something like:

	`docker run --name openseedbox -p 443:443 -e MYSQL_HOST=<mysql host> -e MYSQL_PORT=<mysql port> -e MYSQL_USERNAME=<mysql username> -e MYSQL_PASSWORD=<mysql password> -e GOOGLE_CLIENTID=<the google clientid you got above> -d openseedbox/client`

You should now be able to browse to Openseedbox via `https://hostname-or-ip-of-docker-host`

**Installing the Backend**

Note: this can be on the same or a different server than the frontend. If its on the same server, you *need* to map it to a different port.

1. Create a folder on your host to store the openseedbox data, eg:

	`mkdir /home/user/openseedbox-data`
	
	I would recommend this to be in an encrypted location, however this is not required.

	*Note* If you have multiple backeds running on the same host, do *not* point them to the same data directory. They will clobber each other.

1. The following command will start an openseedbox-server container and map it to port 444 on your host. It will also mount the `openseedbox-data` folder you just created into the container. This is so that your data will persist between container restarts. Take note of OPENSEEDBOX_API_KEY as you'll need this value when adding the node to the frontend:

	`docker run -v /home/user/openseedbox-data:/media/openseedbox --name openseedbox-node1 -p 444:443 -e OPENSEEDBOX_API_KEY=node1 -d openseedbox/server`

**Telling the frontend about the backend**

1. Browse to the frontend and login. The first user will be automatically created as an admin user.
1. Click "Admin" up the top and then go to the Nodes tab.
1. Click "Create new Node"
1. Fill out the form.
   - Name - Whatever you want, its just used in the UI
   - Scheme - Set to "https" since thats what the docker container uses
   - Host / IP Address - Use the *publically accessible* hostname / ip address of the Docker host running the backend container. You also need to include the port. eg `192.168.1.50:444` if `192.168.1.50` is the IP address of the machine running the backend container and its exposed on port 444. Its important to use the public IP address so that your browser can access the node in order to download the files.
   - Webservice API Key: The value of `OPENSEEDBOX_API_KEY` you started the backend container with above
   - Active: Tick this box
1. Click "Create new Node". You should see the uptime, available space etc. Click "Restart Backend" in order to start transmission-daemon.

**Setting up Plans and Users**

There are two ways to do this. The first is to set up some plans and let the users choose them when they first log in. The second is to set up a "dummy" plan and add users to it manually via the web interface. I'll detail the second option here.

1. Go to "Admin" => "Plans". Click "Create new Plan"
1. Fill out the form to set the plan limits. Note that "Monthly Cost" doesnt do anything as the whole Invoicing thing has not been implemented and should probably be removed.
   - Plan Name - What to call your plan, eg "Plan 1"
   - Max. Diskspace (GB) - How much diskspace users on this plan can consume, eg "100"
   - Max. Active Torrents - How many active torrents users on this plan can have running at once, eg "5", or "-1" for unlimited
   - Monthly Cost - just put 0 in this field
   - Total Slots Available - How many users can assign themselves to this plan. eg if you had 500GB of diskspace and you set the Max. Diskspace on the plan to 100GB, then you could support 5 users. Set this to "10" for example
   - Visible to Customers - Whether or not users will see this plan in the list of plans after they log in.
1. Click "Create new Plan"
1. Get your users to log in in order for their accounts to be created in the system.
1. Browse to "Admin" => "Users".
1. Click the "Edit" button next to each user and do the following:
   - Assign them to the plan you just created
   - Set a "Dedicated" node if desired. Using a dedicated node means that every torrent they start will go to the same node, instead of the next node available with enough free space.

These users should now be able to start torrents via the Web UI if they log out / log back in again.

**Using your own SSL certs**

You'll quickly discover that the provided SSL certs are self-signed and will show as invalid in every browser. If you want to use your own certs, run the `openseedbox` and `openseedbox-server` containers with the following options:

	-v /path/to/your/host.key:/src/openseedbox/conf/host.key -v /path/to/your/host.cert:/src/openseedbox/conf/host.cert
	
Configuration Reference
-----------------------

**Frontend**

Configuration is in `openseedbox/conf/application.conf`

* `google.clientid` - The ClientID of your app in the Google Developer Console. Used to enable logins.
* `openseedbox.node.access` - 'http' or 'https'. If you're using SSL on your backend nodes, use https, otherwise use http.
* `openseedbox.errors.mailto` - an email address. Errors will get sent here.
* `openseedbox.errors.mailfrom` - the From address in the error emails
* `openseedbox.zip` - 'true' or 'false'. If 'false', no zip options are shown in the UI
* `openseedbox.zip.path` - the internal NGINX path for multi-torrent zips. Is '/rdr' in the default nginx config.
* `openseedbox.zip.manifestonly` - 'true' or 'false'. Only set to 'true' for debugging multi-torrent zips.
* `openseedbox.assets.prefix` - Used as the prefix to all asset URLs. This is so you can upload all the assets (the /public directory) to something like Amazon S3 and have them served from there.

**Backend**

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
