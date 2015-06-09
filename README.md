Teamcity web-ssh
=================

Description
-----------
Plugin for JetBrains TeamCity for getting secure shell right from web interface.
This innovative plugin adds possibility to access any host, running SSH daemon, directly from your browser. 
Main purpose of it is to simplify access to remote hosts: in case of any problems or necessity to access your build agent
or TeamCity server you can perform this from your browser.

Installation
------------
To install plugin [download zip archive](https://github.com/grundic/teamcity-web-ssh/releases) 
it and copy it to Teamcity \<data directory\>/plugins (it is $HOME/.BuildServer/plugins under Linux and C:\Users\<user_name>\.BuildServer\plugins under Windows). 
For more information, take a look at [official documentation](https://confluence.jetbrains.com/display/TCD9/Installing+Additional+Plugins)

Configuration
-------------
Plugin adds new tab `Web Ssh` under profile page. From there each person can add unlimited number of hosts or presets.
All configured data is stored individually for each person. Host configuration represents single host, to which user could connect.
Preset represents common credentials, that could be shared between multiple hosts.

Host configuration consists of several fields:
  - Preset: if some preset is selected, it would overwrite host credentials with it's own
  - Host: hostname or IP address of a host to connect to
  - Port: port number, usually 22
  - Login: username or login that should be used to connect to remote server
  - Password: password to ssh
  - Private Key: private key to use instead of password

Preset add possibility to share common credentials between multiple hosts. Here is description of it's fields:
  - Name: arbitrary name of the preset
  - Login: login for ssh. It would overwrite host's login.
  - Password: password for ssh. It would overwrite host's password.
  - Private Key: private key for ssh. It would overwrite host's private key.

License
-------
[MIT](https://github.com/grundic/teamcity-web-ssh/blob/master/LICENSE)
