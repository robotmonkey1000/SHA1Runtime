package studio.robotmonkey.sha1runtime;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import studio.robotmonkey.sha1runtime.Commands.Commands;

import java.io.*;


public class SHA1Runtime implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("sha1runtime");

	@Override
	public void onInitialize() {

		LOGGER.info("SHA1Runtime Initializing");
		CommandRegistrationCallback.EVENT.register(Commands::RegisterAllCommands);

	}

}
