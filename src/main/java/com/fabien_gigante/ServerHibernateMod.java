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
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.TickRateManager;

public class ServerHibernateMod implements ModInitializer, ServerPlayConnectionEvents.Join, ServerPlayConnectionEvents.Disconnect,  ServerLifecycleEvents.ServerStarted {
	public static final Logger LOGGER = LoggerFactory.getLogger("server-hibernate");
	private boolean windowsOS;
	private static final float DEFAULT_TICKRATE = 20.0f;

	// Server-side mod entry point
	@Override
	public void onInitialize() {
		LOGGER.info("ServerHibernateMod - Mod starting...");
		String os = System.getProperty("os.name").toLowerCase(); 
		windowsOS = os.contains("win");
		ServerPlayConnectionEvents.JOIN.register(this);
		ServerPlayConnectionEvents.DISCONNECT.register(this);	
		ServerLifecycleEvents.SERVER_STARTED.register(this);
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(Commands.literal("shell")
				.then(Commands.argument("command", StringArgumentType.greedyString())
					.executes(this::onCommandShell)));
		});
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(Commands.literal("meta").executes(this::onCommandMeta));
		});
	}
	
	public void hibernate(MinecraftServer server, boolean hibernate) {
		var tickManager = server.tickRateManager();
		boolean hibernating = tickManager.tickrate() == TickRateManager.MIN_TICKRATE;
		if (hibernate == hibernating) return;
		tickManager.setTickRate(hibernate ? TickRateManager.MIN_TICKRATE : DEFAULT_TICKRATE);
		LOGGER.info("Server is now running at {} tickrate.", hibernate ? "minimum" : "default");
	}

	@Override
	public void onServerStarted(MinecraftServer server) {
		if (server.getPlayerCount() == 0) {
			LOGGER.info("No player connected yet.");
			hibernate(server, true);
		}
	}

	@Override
	public void onPlayDisconnect(ServerGamePacketListenerImpl handler, MinecraftServer server) {
		if (server.getPlayerCount() == 1) {
			LOGGER.info("Last player disconnected.");
			hibernate(server, true);
		}
	}

	@Override
	public void onPlayReady(ServerGamePacketListenerImpl handler, PacketSender sender, MinecraftServer server) {
		if (server.getPlayerCount() == 0) {
			LOGGER.info("First player joined.");
			hibernate(server, false);
		}
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
		Component meta = Component.empty().append(server.getMotd()).append(" ("+server.getServerVersion()+")");
		source.sendSystemMessage(meta);
		return 1;
	}
}