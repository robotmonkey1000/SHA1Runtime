
# SHA1 Runtime

This is a simple Fabric mod that enables you to update the hash for your server resource pack while the server is still running.

If you are anything like me, I like to constantly add new custom models to my resource pack just to add a little extra customization to Vanilla Minecraft. I do not like however having to ask people to stop what they are doing so I can restart the server to get it to read the new SHA1 Hash.

Since a standard server only reads the server.properties file on startup you must restart every time you make a change to your pack and add a new hash. That is why this was created, so I can tweak and add all I want and people can freely join in and out getting the new resource pack *automagically*.
## Download
![Modrinth Downloads](https://img.shields.io/modrinth/dt/YoRtMPzM?color=modrinth&logo=modrinth) [Download on Modrinth](https://modrinth.com/mod/sha1runtime)

[Download on Curseforge](https://www.curseforge.com/minecraft/mc-mods/sha1runtime)
## Mod Setup

Install on a Fabric __server__ only.

1. Once installed make sure your server.properties has a SHA1 Hash assigned and a URL for the resource pack. The hash does not have to be accurate as the config file that is generated will be used to serve your updated hashes.
2. Run the server and join.
3. Whenever you update your resource pack put the new hash in the `config/ResourcePackHash.txt` file or run the `/updatehash` command with the new hash.
4. Once the new hash is added save the file and re-log in game. It should prompt you to install the new resource pack.

## Usage:

Once installed you can edit the `config/ResourcePackHash.txt` file or run the `/updatehash` command with your new hash to update your hash.
You can use `/checkhash` to get some info on which hash is being supplied.

You can also use `/fetchhash` to automagically calculate and set the hash. For a few seconds while the hash is calculated, may cause performance issues if your pack is large.

If you use a third party resource pack host, you can use `/setpackurl` to set the url of the pack without the need to restart the server.

## Commands
`/checkhash` : Will print out info on the current supplied hash.

`/updatehash HASH_EXAMPLE` : Will update the config to contain the supplied hash.

`/fetchhash` : Will hash the resourcepack currently at the URL from the server.properties (Or in override file) and save the hash to the config file.

`/setpackurl URL` : Will update the pack override config to contain the supplied url and override the URL found in the server.properties. This is useful for people who host their packs on third party sites, or want version info in the zip name.
