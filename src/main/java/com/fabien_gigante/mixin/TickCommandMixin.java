package com.fabien_gigante.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.commands.TickCommand;

import com.fabien_gigante.PauseableWhenEmpty;

@Mixin(TickCommand.class)
public class TickCommandMixin {
    @Inject(method = "tickQuery", at = @At("HEAD"))
    private static void onTickQuery(CommandSourceStack source, CallbackInfoReturnable<Integer> cir) {
        if (source.getServer() instanceof PauseableWhenEmpty server && server.isPausedWhenEmpty())
            source.sendSuccess(() -> Component.translatable("commands.tick.status.paused"), false);
    }
}
