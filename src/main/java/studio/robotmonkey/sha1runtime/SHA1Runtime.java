package studio.robotmonkey.sha1runtime;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import studio.robotmonkey.sha1runtime.Commands.Commands;

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
		CommandRegistrationCallback.EVENT.register(Commands::RegisterAllCommands);

	}




}
