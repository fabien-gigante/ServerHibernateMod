package com.fabien_gigante;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.commands.arguments.EntityArgument;

public class ServerHibernateMod implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("server-hibernate");
	private boolean windowsOS;

	// Server-side mod entry point
	@Override
	public void onInitialize() {
		LOGGER.info("ServerHibernateMod - Mod starting...");
		String os = System.getProperty("os.name").toLowerCase(); 
		windowsOS = os.contains("win");
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(Commands.literal("shell")
				.then(Commands.argument("command", StringArgumentType.greedyString())
					.executes(this::onCommandShell)));
		});
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(Commands.literal("meta").executes(this::onCommandMeta));
		});
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(Commands.literal("data").then(Commands.literal("count")
				.then(Commands.argument("entities", EntityArgument.entities())
				.executes(this::onCommandDataCount))));
		});
	}

	private int onCommandShell(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		var source = context.getSource();
		String command = StringArgumentType.getString(context, "command");
		int rc = -1;
		LOGGER.info("Running shell command : {}", command);
		try {
			String[] execCommand = windowsOS ? new String[] {"cmd", "/c", command} : new String[] {"/bin/sh", "-c", command};
			ProcessBuilder builder = new ProcessBuilder(execCommand);
			builder.redirectErrorStream(true);
			Process process = builder.start();
			if (process.waitFor(30, TimeUnit.SECONDS)) rc = process.exitValue();
			BufferedReader buf = new BufferedReader(new InputStreamReader(process.getInputStream()));
			for(String line; (line = buf.readLine()) != null;) source.sendSystemMessage(Component.literal(line));
		}
		catch(Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		return rc == 0 ? 1 : 0; // Return 1 if the command executed successfully
	}

	private int onCommandMeta(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		var source = context.getSource(); 
		MinecraftServer server = source.getServer();
		Component message = Component.empty().append(server.getMotd()).append(" ("+server.getServerVersion()+")");
		source.sendSystemMessage(message);
		return 1;
	}

	private int onCommandDataCount(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		CommandSourceStack source = context.getSource();
		var entities = EntityArgument.getEntities(context, "entities");
		Component message = Component.translatable("commands.data.count").append(String.valueOf(entities.size()));
		source.sendSystemMessage(message);
		return 1;
	}
}
