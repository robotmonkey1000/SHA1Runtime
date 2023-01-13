# SHA1 Runtime

This is a minecraft mod for fabric. 
This makes it so that you never need to restart the server for it to read a new hash for a resource pack every time you make changes to it.

Please read the setup instructions below. 


## Setup

Install on a Fabric __server__ only. 

1. Once installed make sure your server.properties has a SHA1 Hash assigned and a URL for the resource pack. The hash does not have to be accurate.
2. Run the server and join.
3. Whenever you update your resource pack put the new hash in the `config/ResourcePackHash.txt` file.
4. Once the new hash is added save the file and re-log in game. It should prompt you to install the new resource pack.

## Todo:

Setup command to update the hash from in game.