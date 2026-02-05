package com.fabien_gigante.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.server.MinecraftServer;

import com.fabien_gigante.PauseableWhenEmpty;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin implements PauseableWhenEmpty {
    @Shadow int emptyTicks;
    @Shadow abstract int pauseWhenEmptySeconds();

    public boolean isPausedWhenEmpty() {
        int pauseTicks = pauseWhenEmptySeconds() * 20;
        return pauseTicks > 0 && emptyTicks >= pauseTicks;
    }
}