package com.fabien_gigante.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;

@Mixin(MinecraftServer.class)
public interface MinecraftServerAccessor {
    @Accessor("emptyTicks")
    int getEmptyTicks();

    default boolean isPausedWhenEmpty() {
        if (!(this instanceof DedicatedServer server)) return false;
        int pauseTicks = server.pauseWhenEmptySeconds() * 20;
        return pauseTicks > 0 && getEmptyTicks() >= pauseTicks;
    }    
}