# SHA1 Runtime

This is a simple Fabric mod that enables you to update the hash for your server resource pack while the server is still running.

If you are anything like me, I like to constantly add new custom models to my resource pack just to add a little extra customization to Vanilla Minecraft. I do not like however having to ask people to stop what they are doing so I can restart the server to get it to read the new SHA1 Hash.

Since a standard server only reads the server.properties file on startup you must restart every time you make a change to your pack and add a new hash. That is why this was created, so I can tweak and add all I want and people can freely join in and out getting the new resource pack *automagically*.

## Setup

Install on a Fabric __server__ only.

1. Once installed make sure your server.properties has a SHA1 Hash assigned and a URL for the resource pack. The hash does not have to be accurate as the config file that is generated will be used to serve your updated hashes.
2. Run the server and join.
3. Whenever you update your resource pack put the new hash in the `config/ResourcePackHash.txt` file.
4. Once the new hash is added save the file and re-log in game. It should prompt you to install the new resource pack.

## Usage:

Once installed you can edit the `config/ResourcePackHash.txt` file to update your hash.
You can use `/checkhash` to get some info on which hash is being supplied.

## Todo:

Setup command to update the hash from in game.