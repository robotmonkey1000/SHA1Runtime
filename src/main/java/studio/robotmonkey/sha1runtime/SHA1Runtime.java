package studio.robotmonkey.sha1runtime;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Scanner;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class SHA1Runtime implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("sha1runtime");

	@Override
	public void onInitialize() {


		LOGGER.info("SHA1Runtime Initializing");

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(literal("checkhash")
				.executes(context -> {
					// For versions below 1.19, replace "Text.literal" with "new LiteralText".
					if(context.getSource().getServer().getResourcePackProperties().isPresent())
					{
						context.getSource().sendMessage(Text.literal( "Hash in server.properties: " + context.getSource().getServer().getResourcePackProperties().get().hash()));

						File hashFile = new File("config/ResourcePackHash.txt");
						try {
							Scanner fileReader = new Scanner(hashFile);
							if(fileReader.hasNextLine()) {
								String hash = fileReader.nextLine();
								context.getSource().sendMessage(Text.literal("Hash in config file: " + hash));
							} else {
								context.getSource().sendMessage(Text.literal("No Hash in config file: Please open config folder and add your hash."));
							}
							fileReader.close();
						} catch(FileNotFoundException e)
						{
							context.getSource().sendMessage(Text.literal("Config file is missing. Generating a new one now...."));
							SHA1Runtime.LOGGER.warn("Missing Hash File! Generating Now...");
							try {
								boolean created = hashFile.createNewFile();
								if(created) {
									FileWriter writer = new FileWriter(hashFile);
									writer.write(context.getSource().getServer().getResourcePackProperties().get().hash());
									writer.close();
									context.getSource().sendMessage(Text.literal("Config file generated. Update with your new hash when needed."));
								} else {
									SHA1Runtime.LOGGER.error("Could not create config file!");
								}

							} catch(IOException ioException)
							{
								ioException.printStackTrace();
							}
						}
					} else {
						context.getSource().sendMessage(Text.literal("No Hash Present in server.properties make sure you assign one even if it is outdated."));
					}


					return 1;
				})));



		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
			{
				if(environment.dedicated) {
					dispatcher.register(literal("updatehash")
						.requires(source -> source.hasPermissionLevel(4))
						.then(argument("hash", StringArgumentType.greedyString())
							.executes(context ->
							{
								if(context.getSource().getServer().isDedicated()) {
									String hash = getString(context, "hash");

									File config = GetOrCreateConfig();
									try {
										FileWriter writer = new FileWriter(config);
										writer.write(hash);
										writer.close();
										context.getSource().sendMessage(Text.literal("Updated config to: " + hash));
										SHA1Runtime.LOGGER.info("Updated config with new hash: " + hash);
									} catch (IOException e) {
										context.getSource().sendMessage(Text.literal("Failed to update hash!"));
										SHA1Runtime.LOGGER.error("Failed to write new hash!");
									}

								}
								return 1;
							})
						));

					dispatcher.register(literal("fetchhash").requires(source -> source.hasPermissionLevel(4)).executes(context -> {
						if(context.getSource().getServer().isDedicated()) {
							Thread fetchPackThread = new Thread(new Runnable() {
								public void run()
								{
									try
									{
										MinecraftServer.ServerResourcePackProperties props = context.getSource().getServer().getResourcePackProperties().orElseThrow();
										String url = props.url();

										SHA1Runtime.LOGGER.info("Fetching resource pack from: " + url);
										BufferedInputStream in = new BufferedInputStream(new URL(url).openStream());

										MessageDigest digest = MessageDigest.getInstance("SHA-1");
										DigestInputStream stream = new DigestInputStream(in, digest);
										while(stream.read() != -1) {}
										stream.close();

										SHA1Runtime.LOGGER.info("Resource pack fetch completed. Calculating new hash!");
										byte[] hash = digest.digest();
										//SHA1Runtime.LOGGER.info(Arrays.toString(hash));
										StringBuilder hexString = new StringBuilder();

										for (byte b : hash) {
											hexString.append(String.format("%02x", b));
										}

										File config = GetOrCreateConfig();
										FileWriter writer = new FileWriter(config);
										writer.write(hexString.toString().toUpperCase());
										writer.close();
										context.getSource().sendMessage(Text.literal("Updated config to: " + hexString.toString().toUpperCase()));
										SHA1Runtime.LOGGER.info("Updated config with new hash: " + hexString.toString().toUpperCase());

									} catch(NoSuchElementException e)
									{
										context.getSource().sendMessage(Text.literal("No resource pack properties found. Make sure hash and url are set."));
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
								}});

							fetchPackThread.start();


						}
						return 1;
					}));
				}

			}
		);

	}

	public static File GetOrCreateConfig() {
		File hashFile = new File("config/ResourcePackHash.txt");
		if(!hashFile.exists()) {
			SHA1Runtime.LOGGER.warn("Missing Hash File! Generating Now...");
			try {
				boolean created = hashFile.createNewFile();
				if(created) {
					FileWriter writer = new FileWriter(hashFile);
					writer.write("");
					writer.close();
					SHA1Runtime.LOGGER.info("Config file generated. Update with your new hash when needed.");
				} else {
					SHA1Runtime.LOGGER.error("Could not create config file!");
				}

			} catch(IOException ioException)
			{
				ioException.printStackTrace();
			}
		}
		return hashFile;
	}


}
