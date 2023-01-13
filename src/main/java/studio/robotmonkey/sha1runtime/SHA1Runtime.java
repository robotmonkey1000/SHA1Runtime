package studio.robotmonkey.sha1runtime;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

import static net.minecraft.server.command.CommandManager.*;

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
	}

}
