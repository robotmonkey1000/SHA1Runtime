package studio.robotmonkey.sha1runtime.Commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.network.protocol.common.ClientboundResourcePackPushPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import studio.robotmonkey.sha1runtime.SHA1Runtime;
import studio.robotmonkey.sha1runtime.Util.Util;

import java.io.*;
import java.net.URL;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Scanner;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;
import static studio.robotmonkey.sha1runtime.Util.Util.GetOrCreateConfig;
import static studio.robotmonkey.sha1runtime.Util.Util.GetOrCreateUrlOverride;

public class Commands {
    public static void RegisterAllCommands(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext registryAccess, net.minecraft.commands.Commands.CommandSelection env)
    {
        //Server sided commands
        if(env.includeDedicated)
        {
            CheckHash.register(dispatcher);
            UpdateHash.register(dispatcher);
            Reload.register(dispatcher);
            FetchHash.register(dispatcher);
            FetchHashReload.register(dispatcher);
            UpdateURL.register(dispatcher);
        }
    }

    public static class CheckHash {
        private static final String command = "checkhash";
        public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
            dispatcher.register(literal(command).executes(CheckHash::execute));
        }
        public static int execute(CommandContext<CommandSourceStack> context) {
            if(context.getSource().getServer().getServerResourcePack().isPresent())
            {
                context.getSource().sendSystemMessage(Component.literal( "Hash in server.properties: " + context.getSource().getServer().getServerResourcePack().get().hash()));

                File hashFile = new File("config/ResourcePackHash.txt");
                try {
                    Scanner fileReader = new Scanner(hashFile);
                    if(fileReader.hasNextLine()) {
                        String hash = fileReader.nextLine();
                        context.getSource().sendSystemMessage(Component.literal("Hash in config file: " + hash));
                    } else {
                        context.getSource().sendSystemMessage(Component.literal("No Hash in config file: Please open config folder and add your hash."));
                    }
                    fileReader.close();
                } catch(FileNotFoundException e)
                {
                    context.getSource().sendSystemMessage(Component.literal("Config file is missing. Generating a new one now...."));
                    SHA1Runtime.LOGGER.warn("Missing Hash File! Generating Now...");
                    try {
                        boolean created = hashFile.createNewFile();
                        if(created) {
                            FileWriter writer = new FileWriter(hashFile);
                            writer.write(context.getSource().getServer().getServerResourcePack().get().hash());
                            writer.close();
                            context.getSource().sendSystemMessage(Component.literal("Config file generated. Update with your new hash when needed or use /fetchhash to automatically set it."));
                        } else {
                            SHA1Runtime.LOGGER.error("Could not create config file!");
                        }

                    } catch(IOException ioException)
                    {
                        ioException.printStackTrace();
                    }
                }
            } else {
                context.getSource().sendSystemMessage(Component.literal("No Hash Present in server.properties make sure you assign one even if it is outdated."));
            }

            return 1;
        }
    }

    public static class UpdateHash {
        private static final String command = "updatehash";
        public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
            dispatcher.register(literal(command)
                    .requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_OWNER))
                    .then(argument("hash", StringArgumentType.greedyString())
                            .executes(UpdateHash::execute)
                    ));
        }
        public static int execute(CommandContext<CommandSourceStack> context) {
            if(context.getSource().getServer().isDedicatedServer()) {
                String hash = getString(context, "hash");
                File config = GetOrCreateConfig();
                try {
                    FileWriter writer = new FileWriter(config);
                    writer.write(hash);
                    writer.close();
                    context.getSource().sendSystemMessage(Component.literal("Updated config to: " + hash));
                    SHA1Runtime.LOGGER.info("Updated config with new hash: " + hash);
                } catch (IOException e) {
                    context.getSource().sendSystemMessage(Component.literal("Failed to update hash!"));
                    SHA1Runtime.LOGGER.error("Failed to write new hash!");
                }

            }
            return 1;
        }
    }

    public static class Reload {
        private static final String command = "reload_pack";

        public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
            dispatcher.register(literal(command).requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_OWNER)).executes(Commands.Reload::execute));
        }

        public static int execute(CommandContext<CommandSourceStack> context) {
            if (context.getSource().getServer().isDedicatedServer()) {
                Optional<MinecraftServer.ServerResourcePackInfo> props = context.getSource().getServer().getServerResourcePack();
                if(props.isPresent()) {
                    String url = props.get().url();
                    //String hash = Util.GetHash(); //Not required as handled by the mixin
                        if(Util.IsOverrideSet())
                        {
                            //URL override, use from file
                            url = Util.GetURLFromConfig();
                        }
                        Component prompt = Component.nullToEmpty("Reloading resource pack!");
                        for (var player : context.getSource().getServer().getPlayerList().getPlayers()) {
                            player.connection.send(new ClientboundResourcePackPushPacket(props.get().id(), url, props.get().hash(), true, Optional.of(prompt)));
                        }
                }
            }

            return 1;
        }
    }

    public static class FetchHashReload {
        private static final String command = "fetchhash";

        public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
            dispatcher.register(literal(command).requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_OWNER)).then(literal("reload").executes(Commands.FetchHashReload::execute)));
        }

        public static int execute(CommandContext<CommandSourceStack> context) {
            if (context.getSource().getServer().isDedicatedServer()) {
                FetchHash.run(context,true);
            }

            return 1;
        }
    }

    public static class FetchHash {
        private static final String command = "fetchhash";
        public static void register(CommandDispatcher<CommandSourceStack> dispatcher)
        {
            dispatcher.register(literal(command).requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_OWNER)).executes(FetchHash::execute));
        }

        public static int run(CommandContext<CommandSourceStack> context, boolean reload)
        {
            Thread fetchPackThread = new Thread(() -> {
                try
                {
                    //TODO check to see if URL override is set and resolve it instead of the URL of the
                    MinecraftServer.ServerResourcePackInfo props = context.getSource().getServer().getServerResourcePack().orElseThrow();
                    String url = props.url();
                    if(Util.IsOverrideSet())
                    {
                        SHA1Runtime.LOGGER.info("URL Override Set in Config.");
                        url = Util.GetURLFromConfig();
                    }
                    SHA1Runtime.LOGGER.info("Fetching resource pack from: " + url);
                    BufferedInputStream in = new BufferedInputStream(new URL(url).openStream());

                    MessageDigest digest = MessageDigest.getInstance("SHA-1");
                    DigestInputStream stream = new DigestInputStream(in, digest);
                    while(stream.read() != -1) {} //Required to read the entire stream from the buffer;
                    stream.close();

                    SHA1Runtime.LOGGER.info("Resource pack fetch completed. Calculating new hash!");
                    byte[] hash = digest.digest();
                    StringBuilder hexString = new StringBuilder();

                    for (byte b : hash) {
                        hexString.append(String.format("%02x", b));
                    }

                    File config = GetOrCreateConfig();
                    FileWriter writer = new FileWriter(config);
                    writer.write(hexString.toString().toUpperCase());
                    writer.close();
                    context.getSource().sendSystemMessage(Component.literal("Updated config to: " + hexString.toString().toUpperCase()));
                    SHA1Runtime.LOGGER.info("Updated config with new hash: " + hexString.toString().toUpperCase());

                } catch(NoSuchElementException e)
                {
                    context.getSource().sendSystemMessage(Component.literal("No resource pack properties found. Make sure hash and url are set."));
                    SHA1Runtime.LOGGER.error("No resource pack properties found. Make sure hash and url are set.");
                }
                catch (IOException e)
                {
                    SHA1Runtime.LOGGER.error("IOException: " + e);
                }
                catch(NoSuchAlgorithmException algo)
                {
                    SHA1Runtime.LOGGER.error("Wrong algorithm!");
                }

                if(reload)
                {
                   Reload.execute(context);
                }
            });

            fetchPackThread.start();

            return 1;
        }
        public static int execute(CommandContext<CommandSourceStack> context)
        {
            if(context.getSource().getServer().isDedicatedServer())
            {
                run(context, false);
            }
            return 1;
        }
    }
    public static class UpdateURL {
        private static final String command = "setpackurl";
        public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
            dispatcher.register(literal(command)
                    .requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_OWNER))
                    .then(argument("url", StringArgumentType.greedyString())
                            .executes(UpdateURL::execute)
                    ));
        }
        public static int execute(CommandContext<CommandSourceStack> context) {
            if(context.getSource().getServer().isDedicatedServer()) {
                String url = getString(context, "url");
                File config = GetOrCreateUrlOverride();
                try {
                    FileWriter writer = new FileWriter(config);
                    writer.write(url);
                    writer.close();
                    context.getSource().sendSystemMessage(Component.literal("Updated URL config to: " + url));
                    SHA1Runtime.LOGGER.info("Updated config with new url: " + url);
                } catch (IOException e) {
                    context.getSource().sendSystemMessage(Component.literal("Failed to update URL!"));
                    SHA1Runtime.LOGGER.error("Failed to write new URL!");
                    SHA1Runtime.LOGGER.error(e.toString());
                }

            }
            return 1;
        }
    }
}
