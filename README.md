OpenSeedbox - Open Source Seedbox UI - https://www.openseedbox.com

Comprised of the following projects:

* Common Code (openseedbox-common) : https://github.com/openseedbox/openseedbox-common
* Frontend (openseedbox) : https://github.com/openseedbox/openseedbox
* Backend (openseedbox-server) : https://github.com/openseedbox/openseedbox-server

Overview
--------
OpenSeedbox is a web-based UI for bittorrent. It is designed to be multi-user and make use of existing bittorrent clients to do the actual downloading. Currently only transmission-daemon is supported as a backend but more can be implemented via the ITorrentBackend interface.


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

* OpenJDK >= 1.6 & < 1.9
* Play! Framework >= 1.3.4 & < 1.5
* transmission-daemon >= 2.51 (backend only)
* Nginx compiled with mod_zip and headers_more module (if you want zip support), otherwise Apache will work too (youll need X-Sendfile support if you use Apache)

[Architecture](https://github.com/gregorkistler/openseedbox/wiki/Architecture)  
[Installation](https://github.com/gregorkistler/openseedbox/wiki/Installation)  
[Configuration Reference](https://github.com/gregorkistler/openseedbox/wiki/Configuration-Reference)  
