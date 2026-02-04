package com.fabien_gigante.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.commands.TickCommand;
import net.minecraft.server.dedicated.DedicatedServer;

@Mixin(TickCommand.class)
public class TickCommandMixin {
    @Inject(method = "tickQuery", at = @At("HEAD"), cancellable = true)
    private static void onTickQuery(CommandSourceStack source, CallbackInfoReturnable<Integer> cir) {
        if (!(source.getServer() instanceof DedicatedServer server)) return;
        int pauseTicks = server.pauseWhenEmptySeconds() * 20;
        int emptyTicks = ((MinecraftServerAccessor) server).getEmptyTicks();
        if (pauseTicks > 0 && emptyTicks >= pauseTicks)
            source.sendSuccess(() -> Component.translatable("commands.tick.status.paused"), false);          
    }   
}
