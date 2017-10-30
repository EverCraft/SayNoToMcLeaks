SayNoToMcLeaks
=============

## Description ##
This plugin block Alt Account System same as McLeaks or AltDispenser.

SayNoToMcLeaks uses MojangAPI to check IP Authentication on Minecraft.net and on your server.
If the result doesnt the same, the plugin execute custom commands (Editable on config file).

Please share SayNoToMcLeaks to your friends or others because
more SayNoToMcLeaks will be installed on Minecraft servers.
More McLeaks and Alt account system will be useless.
A minecraft plugin for Bukkit platforms.

[More informations](wiki.vg)

## Stolen Account ##
![ClientNoSafe](doc/ClientNoSafe.png)

## Legal Account ##
![ClientSafe](doc/ClientSafe.png)

## Installation with Bukkit ##
1. Download the plugin (The plugin contains Bukkit and BungeeCord version)
2. Download ProtocolLib
3. Put then in the Plugins folder in your Bukkit server
4. Reload/Restart your Bukkit server
5. If you are using a BungeeCord server, modify the file "plugins/SayNoToMcLeaks/config.yml " by changing the value "bungeecord" to True. Reload/Restart your Bukkit server. (Please note that it will be necessary to install the plugin on the Bungee server)
6. You can also change the value "commands", which contains the list of commands that will be executed by the Bukkit console. Then, to reload the list just do "/mcleaks reload"

## Installation with BungeeCord or Waterfall ##
1. Download the plugin (The plugin contains Bukkit and BungeeCord version)
2. Put then in the Plugin folder in your Bungee server
3. Restart your BungeeCord server
4. To change the list of commands executed by the Bungee console, change the "commands" value in the file "plugins/SayNoToMcLeaks/config.yml ". Then, to reload the list just do "/bmcleaks reload"
5. If you want to run Bukkit commands, you must also install the plugin on your Bukkit servers and then configure the command list directly in the Bukkit server configuration file

## Links ##
* [Download](https://www.spigotmc.org/resources/saynotomcleaks.40906/download?version=161748)
* [Issues](https://github.com/EverCraft/SayNoToMcLeaks/issues)
* [Website](http://evercraft.fr)
* [Support Me](https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=RUSKPBMNJG5R4)

## License ##
This plugin is licensed under [GNU License](https://github.com/EverCraft/SayNoToMcLeaks/blob/master/LICENSE).

## Prerequisites ##
* [Java 7](http://www.oracle.com/technetwork/java/javase/downloads/jdk7-downloads-1880260.html)
* [ProtocolLib](https://www.spigotmc.org/resources/protocollib.1997/)

## Commands ##
* /SayNoToMcLeaks reload : Reload configuration
* /bSayNoToMcLeaks reload : Reload configuration (BungeeCord Version)

## Permission ##
* saynotomcleaks.reload : Permission to reload this plugin
* saynotomcleaks.bypass : Permission to bypass Alt Account protection

## Configurations Example ##
    # ##############################################################
    #                    SayNoToMcLeaks (By rexbut)                #
    # ##############################################################
    #
    #  Debug mode display messages on console
    debug: false
    # bungeecord : Do you use a Bungeecord
    bungeecord: false
    # Executes commands by Bukkit console
    # --------------------------------------------------------------
    # | Replace variable   |  Example                              |
    # --------------------------------------------------------------
    # |  <player>          |  lesbleu                              |
    # |  <uuid>            |  f3345769-4c70-4a9f-9db9-bdb8f9e8a46c |
    # |  <displayname>     |  Lesbleu                              |
    # |  <ip>              |  123.80.10.2                          |
    # --------------------------------------------------------------
    commands:
    - 'kick <player> &cAlt Account detected'
    - 'broadcast &6<player> &7was kicked for reason : Alt Account detected'

## Statistics ##
SayNoToMcLeaks collects statistics anonymously through [bStats](https://bstats.org/plugin/bukkit/SayNoToMcLeaks).

## Clone ##
The following steps will ensure your project is cloned properly

1. `git clone --recurse git@github.com:EverCraft/SayNoToMcLeaks.git`
2. `cd SayNoToMcLeaks`

## Building ##

### On Windows ###

1. Shift + right click the folder with SayNoToMcLeaks's files and click "Open command prompt".
2. `gradlew clean`
3. `gradlew build`
4. The plugins are located in the folder '/folder/of/SayNoToMcLeaks/build/libs/'

### On Linux or Mac OS X ###

1. In your terminal, navigate to the folder with SayNoToMcLeaks's files (`cd /folder/of/SayNoToMcLeaks/`)
2. `./gradlew clean`
3. `./gradlew build`
4. The plugins are located in the folder '/folder/of/SayNoToMcLeaks/build/libs/'
